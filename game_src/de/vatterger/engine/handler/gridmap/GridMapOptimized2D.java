
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

/** GridMap2D sorts entities (id, x, y, [radius, flag]) into equally distributed same sized buckets/cells, allowing for fast
 * tagged sub-region queries like:
 * <ul>
 * <li>Get all entity-ids inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all entity-ids and their collision-data (x, y, radius) inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all entity-ids inside Rectangle(x1,y1,x2,y2) with flag 0x64</li>
 * </ul>
 * <p>
 * The entities can be tagged with a bit-flag and filtered by this bit-flag to only return specific types of entities.
 * <p>
 * Remember to set the cell size at least as large as the largest possible radius of any inserted entity.
 * @author VaTTeRGeR */
public class GridMapOptimized2D {

	/** marks the buckets storage space as not yet allocated. */
	private static final int UNALLOCATED = -1;
	/** marks the buckets storage space as not yet allocated. */
	private static final int BUCKET_NULL = -1;

	/** bucket-index -> pointer into shared storage memory (*Mem). */
	private final int[] pointerMap;
	/** bucket-index -> bucket-size. */
	private final int[] sizeMap;
	/** bucket-index -> bucket-capacity. */
	private final int[] capacityMap;

	/** shared storage: entity ID. */
	private int[] eidMem;
	/** shared storage: entity Flags. */
	private int[] flagMem;

	/** shared storage: entity x-position. */
	private float[] xMem;
	/** shared storage: entity y-position. */
	private float[] yMem;
	/** shared storage: entity collision-radius. */
	private float[] rMem;

	/** entity id -> bucket-index. Allows updating and removing entries. */
	private int[] eidToBucketMem;

	/** the amount of shared memory that is currently used. */
	private int memSize;
	/** the capacity of the shared memory. */
	private int memCapacity;

	/** number of cells in x and y direction. */
	private final int cellsXY;

	/** x and y cell-dimensions in world coordinates. */
	private final int cellSize;

	/** inverse of x and y cell-dimensions in world coordinates. */
	private final float cellSizeInv;

	/** newly created buckets will have this capacity. */
	private final int initialBucketSize;

	/** x-offset in world coordinates. */
	private float offsetX;
	/** y-offset in world coordinates. */
	private float offsetY;

	/** pre-calculated upper bounds of this GridMap (->rectangle) */
	private float x2, y2;

	/** enabling preallocation allows the buckets to be sorted. */
	private final boolean sorted;

	/** Constructs a GridMap covering a rectangle with a width and height of (cellsXY * cellSize) units.
	 * <p>
	 * Remember to set the cellSize at least as large as the largest possible radius of any inserted entity.
	 * @param cellsXY The number of cells/buckets in X and Y direction.
	 * @param cellSize The size of each bucket/cell.
	 * @param initialBucketSize A previously unused bucket will have this initial capacity. Higher values mean less bucket resizing
	 *           (faster in the beginning) but more memory usage.
	 * @param preallocate If true every bucket will be initialized with a capacity equal to initialBucketSize. If false the buckets
	 *           will be lazily initialized. Preallocation allows a sequential memory layout for consecutive buckets, without
	 *           preallocation the buckets will be allocated on demand in order of entity insertion. Use preallocation on static
	 *           content. */
	public GridMapOptimized2D (int cellsXY, int cellSize, int initialBucketSize, boolean preallocate) {

		if (cellsXY < 1 || cellsXY > Short.MAX_VALUE) {
			throw new IllegalArgumentException("cellsXY out of range [1," + Short.MAX_VALUE + "]:" + cellsXY);
		}

		if (cellSize < 1) {
			throw new IllegalArgumentException("cellsSize out of range [1," + Integer.MAX_VALUE + "]:" + cellSize);
		}

		if (initialBucketSize < 1) {
			throw new IllegalArgumentException("initialBucketSize out of range [1," + Integer.MAX_VALUE + "]:" + cellSize);
		}

		this.cellsXY = cellsXY;

		this.cellSize = cellSize;
		this.cellSizeInv = 1f / cellSize;

		this.initialBucketSize = initialBucketSize;

		final int totalCells = cellsXY * cellsXY;

		// Buckets
		pointerMap = new int[totalCells];
		sizeMap = new int[totalCells];
		capacityMap = new int[totalCells];

		final int sharedSlots = initialBucketSize * totalCells;

		// Contents of the buckets
		eidMem = new int[sharedSlots];
		flagMem = new int[sharedSlots];
		xMem = new float[sharedSlots];
		yMem = new float[sharedSlots];
		rMem = new float[sharedSlots];
		
		eidToBucketMem = new int[sharedSlots];

		// buckets with no active storage get initialized as -1 and have zero size and capacity.
		Arrays.fill(pointerMap, UNALLOCATED);
		Arrays.fill(eidToBucketMem, BUCKET_NULL);

		memSize = 0;
		memCapacity = sharedSlots;

		this.sorted = preallocate;

		if (preallocate) {
			for (int i = 0; i < totalCells; i++) {
				allocateNewBucket(i, initialBucketSize);
			}
		}

		setOffset(0f, 0f);
	}

	/** Clears all stored entities. */
	public void clear () {
		Arrays.fill(sizeMap, 0);
	}

	/** Fills the provided {@link IntArray} with all entity-ids near the specified point.
	 * @param x X-Coordinate of the point.
	 * @param y Y-Coordinate of the point.
	 * @param bag The {@link IntArray} that will contain the found entity-ids afterwards. */
	public void getIdOnly (float x, float y, IntArray bag, FloatArray packedCollisionArray) {
		get(x, y, x, y, 0, bag, packedCollisionArray);
	}

	/** Fills the provided {@link IntArray} with entity-ids near the specified point and have the specified bit-flags set.
	 * @param x X-Coordinate of the point.
	 * @param y Y-Coordinate of the point.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param bag The {@link IntArray} that will contain the found entity-ids afterwards. */
	public void getIdOnly (float x, float y, int gf, IntArray bag, FloatArray packedCollisionArray) {
		get(x, y, x, y, gf, bag, packedCollisionArray);
	}

	/** Fills the provided {@link IntArray} with all entity-ids that fall inside the specified rectangle [x1,y1,x2,y2].
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param bag The {@link IntArray} that will contain the found entity-ids afterwards. */
	public void get (float x1, float y1, float x2, float y2, IntArray bag, FloatArray packedCollisionArray) {
		get(x1, y1, x2, y2, 0, bag, packedCollisionArray);
	}

	/** Fills the provided {@link IntArray} with entity-ids that fall inside the specified rectangle [x1,y1,x2,y2] and have the
	 * specified bit-flags set and fills the provided {@link FloatArray} with their corresponding collision info in packed form [x0,y0,r0,x1,y1,r1...].
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param bag The {@link IntArray} that will contain the found entity-ids afterwards. */
	public void get (float x1, float y1, float x2, float y2, int gf, IntArray entityIdArray, FloatArray packedCollisionArray) {

		// Return nothing if the query region is out of bounds
		if (x2 < offsetX || y2 < offsetY || x1 >= this.x2 || y1 >= this.y2) {
			return;
		}

		final int bucketX1 = Math.max((int)((x1 - offsetX) * cellSizeInv) - 1, 0);
		final int bucketY1 = Math.max((int)((y1 - offsetY) * cellSizeInv) - 1, 0);
		final int bucketX2 = Math.min((int)((x2 - offsetX) * cellSizeInv) + 1, cellsXY - 1);
		final int bucketY2 = Math.min((int)((y2 - offsetY) * cellSizeInv) + 1, cellsXY - 1);

		final boolean eidArrayPresent = entityIdArray != null;
		final boolean colArrayPresent = packedCollisionArray != null;
		
		for (int bucketY = bucketY1; bucketY <= bucketY2; bucketY++) {

			final int bucketX_start = bucketY * cellsXY + bucketX1;
			final int bucketX_end = bucketY * cellsXY + bucketX2;

			for (int bucketX = bucketX_start; bucketX <= bucketX_end; bucketX++) {

				final int pointer_start = pointerMap[bucketX];
				final int pointer_end = pointer_start + sizeMap[bucketX];

				for (int p = pointer_start; p < pointer_end; p++) {

					if (!GridMapUtil.isContaining(flagMem[p], gf)) {
						continue;
					}

					if(eidArrayPresent) {
						entityIdArray.add(eidMem[p]);
					}
					
					if(colArrayPresent) {
						packedCollisionArray.add(xMem[p], yMem[p], rMem[p]);
					}
				}
			}
		}
	}

	/** Inserts the entity with the specified data into this {@link GridMapOptimized2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y) {
		return put(e, x, y, 0f, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMapOptimized2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y, float r) {
		return put(e, x, y, r, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMapOptimized2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @param gf The flags assigned to the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y, float r, int gf) {

		final int bucketIndex = xyToIndex(x, y);

		if (bucketIndex < 0) return false;

		final int sharedMemPointer;

		if (pointerMap[bucketIndex] != UNALLOCATED) {
			sharedMemPointer = pointerMap[bucketIndex];
		} else {
			sharedMemPointer = allocateNewBucket(bucketIndex, initialBucketSize);
		}

		// If the bucket is full we first try to steal memory from adjacent cells
		// and only if not successful it will resort to expensive full memory shifting.
		if (sizeMap[bucketIndex] == capacityMap[bucketIndex]) {
			if (!tryStealMemory(bucketIndex, 1, 9)) {
				growBucket(bucketIndex, capacityMap[bucketIndex]);
			}
		}

		final int sharedMemCellPointer = sharedMemPointer + sizeMap[bucketIndex];

		sizeMap[bucketIndex]++;

		eidMem[sharedMemCellPointer] = e;
		flagMem[sharedMemCellPointer] = gf;

		xMem[sharedMemCellPointer] = x;
		yMem[sharedMemCellPointer] = y;
		rMem[sharedMemCellPointer] = r;
		
		final int eidToBucketMemLen = eidToBucketMem.length;
		
		if(eidToBucketMemLen <= e) {
			eidToBucketMem = Arrays.copyOf(eidToBucketMem, Math.max(e, eidToBucketMemLen * 2));
			Arrays.fill(eidToBucketMem, eidToBucketMemLen, eidToBucketMem.length, BUCKET_NULL);
		}
		
		eidToBucketMem[e] = bucketIndex;

		return true;
	}
	
	/**
	 * Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found.
	 */
	public final boolean remove(int e) {
	
		final int bucketIndex = eidToBucketMem[e];
		
		// The entry has not been inserted yet and is therefore removed.
		if(bucketIndex == BUCKET_NULL) {
			return true;
		}
		
		eidToBucketMem[e] = BUCKET_NULL;
		
		int pointerEntity = getPointerToEntity(e,bucketIndex);
		
		// The entry was not found in its bucket
		if(pointerEntity == -1) {
			return false;
		}
		
		final int bucketSize = sizeMap[bucketIndex];
		final int pointerLastEntry = pointerMap[bucketIndex] + bucketSize;

		// If this is the only or last entry we just decrement size
		// If not we swap e with the last entry in the bucket
		if(bucketSize > 1 && pointerEntity < pointerLastEntry) {
			eidMem[pointerEntity]	= eidMem[pointerLastEntry];
			flagMem[pointerEntity]	= flagMem[pointerLastEntry];
			xMem[pointerEntity]		= xMem[pointerLastEntry];
			yMem[pointerEntity]		= yMem[pointerLastEntry];
			rMem[pointerEntity]		= rMem[pointerLastEntry];
		}
		
		sizeMap[bucketIndex]--;
		
		return true;
	}
	
	/**
	 * Updates the bucket assignment of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity could not be found.
	 */
	public boolean update(int e, float x, float y) {
		
		final int bucketIndex = eidToBucketMem[e];
		
		if(bucketIndex == BUCKET_NULL) {
			return false;
		}
		
		if(bucketIndex == xyToIndex(x, y)) {
			return true;
		}
		
		int pointer = getPointerToEntity(e, bucketIndex);
		
		if(pointer == -1) {
			return false;
		}
		
		remove(e);
		
		final int gf = flagMem[pointer];
		final float r = rMem[pointer];
		
		put(e, x, y, r, gf);
		
		return true;
	}
	
	/**
	 * Returns the absolute index of the entity.
	 * @param e The id of the entity to search for.
	 * @param bucketIndex The bucket to search in. Calculate this with {@link #xyToIndex(float, float)}
	 * @return The index to the entities data inside the shared memory or -1 if not found.
	 */
	private final int getPointerToEntity (int e, int bucketIndex) {

		final int bucketSize = sizeMap[bucketIndex];
		
		final int pointerStart = pointerMap[bucketIndex];
		final int pointerEnd = pointerMap[bucketIndex] + bucketSize;
		
		// Frequently removed and inserted entries end up towards the end of the bucket.
		for (int i = pointerEnd; i >= pointerStart; i--) {
			
			if(eidMem[i] == e) {
				return i;
			}
		}
		
		return -1;
	}

	/** Allocates space in the shared memory for this bucket and assigns the pointer and capacity.
	 * @param bucketIndex The buckets positional address.
	 * @param bucketSize The initial size of the bucket.
	 * @return The start index of the allocated memory block. */
	private final int allocateNewBucket (int bucketIndex, int bucketSize) {

		final int pointer = memSize;

		pointerMap[bucketIndex] = pointer;

		// make sure the shared memory is large enough, otherwise double it
		while (memSize + bucketSize > memCapacity) {
			growSharedMemory(memCapacity);
		}

		memSize += bucketSize;

		sizeMap[bucketIndex] = 0;
		capacityMap[bucketIndex] = bucketSize;

		return pointer;
	}

	/** Tries to steal memory from adjacent buckets to avoid copying large memory sections for a bucket resize.
	 * @param bucketIndex The bucket that needs more memory.
	 * @param stealAmount The amount of memory it needs.
	 * @param recursiveDepth Number of additional neighbors (apart from the direct neighbor) to ask for memory. Zero means only ask
	 *           the right neighbor.
	 * @return True if the operation was successful, false if not enough memory could be stolen. False also means that no resizing
	 *         occurred. */
	private final boolean tryStealMemory (int bucketIndex, int stealAmount, int recursiveDepth) {

		// Buckets need to be sequential for this to be fast. Otherwise we need to determine neighbors first.
		if (!sorted || recursiveDepth < 0) return false;

		//Profiler p_steal = new Profiler("Mem Steal", TimeUnit.NANOSECONDS);

		final int nextBucketIndex = bucketIndex + 1;

		if (nextBucketIndex < pointerMap.length) {

			final int nextBucketSize = sizeMap[nextBucketIndex];
			int nextBucketCapacity = capacityMap[nextBucketIndex];

			if (nextBucketCapacity <= stealAmount || nextBucketCapacity - nextBucketSize < stealAmount) {

				if (tryStealMemory(nextBucketIndex, stealAmount, recursiveDepth - 1)) {
					nextBucketCapacity += stealAmount;

				} else {
					return false;
				}
			}

			final int nextBucketPointer = pointerMap[nextBucketIndex];
			final int nextBucketPointerNew = pointerMap[nextBucketIndex] + stealAmount;
			final int nextBucketRemainingCapacity = nextBucketCapacity - stealAmount;

			// Shift the pointer of the bucket we stole from
			pointerMap[nextBucketIndex] = nextBucketPointerNew;

			// Shift over the stolen capacity
			capacityMap[bucketIndex] += stealAmount;
			capacityMap[nextBucketIndex] = nextBucketRemainingCapacity;

			// Move the next bucket away from the space that was stolen
			System.arraycopy(eidMem, nextBucketPointer, eidMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(flagMem, nextBucketPointer, flagMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(xMem, nextBucketPointer, xMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(yMem, nextBucketPointer, yMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(rMem, nextBucketPointer, rMem, nextBucketPointerNew, nextBucketRemainingCapacity);

			/*if(recursiveDepth == 9)
				p_steal.log();*/

			return true;
		}

		return false;
	}

	/** Enlarges a single bucket by copying succeeding bucket-contents into a later portion of their backing arrays.<br>
	 * This may also cause a call to {@link #growSharedMemory} that doubles the backing arrays.
	 * @param bucketIndex
	 * @param growAmount */
	private final void growBucket (int bucketIndex, int growAmount) {

		//Profiler p_grow_bucket = new Profiler("Grow Bucket " + bucketIndex, TimeUnit.NANOSECONDS);

		if (growAmount > getFreeMemory()) {
			growSharedMemory(memCapacity);
		}

		// This is the start of the memory section behind this bucket
		final int succeedingBucketPointer = pointerMap[bucketIndex] + capacityMap[bucketIndex];

		capacityMap[bucketIndex] += growAmount;

		// This is the new start of the memory section that gets moved
		final int succeedingBucketPointerNew = succeedingBucketPointer + growAmount;

		// This is the number of bytes that need to be moved
		final int moveAmount = memSize - succeedingBucketPointer;

		memSize += growAmount;

		if (moveAmount > 0) {

			// Move the content pointers of all buckets that succeed the one that has grown.
			for (int i = 0; i < pointerMap.length; i++) {
				if (pointerMap[i] >= succeedingBucketPointer) {
					pointerMap[i] += growAmount;
				}
			}

			System.arraycopy(eidMem, succeedingBucketPointer, eidMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(flagMem, succeedingBucketPointer, flagMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(xMem, succeedingBucketPointer, xMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(yMem, succeedingBucketPointer, yMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(rMem, succeedingBucketPointer, rMem, succeedingBucketPointerNew, moveAmount);
		}

		//p_grow_bucket.log();
	}

	/** Enlarges the shared memory space by creating a new set of backing arrays and copying over the old content.
	 * @param growAmount The amount of additional slots needed. */
	private final void growSharedMemory (int growAmount) {

		//Profiler p_grow_shared = new Profiler("Grow Mem", TimeUnit.NANOSECONDS);

		memCapacity += growAmount;

		eidMem = Arrays.copyOf(eidMem, memCapacity);
		flagMem = Arrays.copyOf(flagMem, memCapacity);
		xMem = Arrays.copyOf(xMem, memCapacity);
		yMem = Arrays.copyOf(yMem, memCapacity);
		rMem = Arrays.copyOf(rMem, memCapacity);

		//p_grow_shared.log();
	}

	/** Calculates the free space inside the shared storage, measured as the number of unused slots after the last bucket.
	 * @return The amount of free space in the shared arrays. */
	private final int getFreeMemory () {
		return memCapacity - memSize;
	}

	/** Sets the world offset of this GridMap. Remember to call {@link #clear()} after changing the offset with entities still
	 * residing in the map.
	 * @param offsetX The offset in x-direction.
	 * @param offsetY The offset in y-direction. */
	public final void setOffset (float offsetX, float offsetY) {

		this.offsetX = offsetX;
		this.offsetY = offsetY;

		final float dimensions = cellSize * cellsXY;

		x2 = offsetX + dimensions;
		y2 = offsetY + dimensions;
	}

	/** Calculates the index of the requested bucket from the supplied world coordinates.
	 * <p>
	 * Pseudo-Formula: i = width * y / cellSize + x / cellSize
	 * @param x The x coordinate in world coordinates.
	 * @param y The y coordinate in world coordinates.
	 * @return The index inside the {@link #pointerMap} array or -1 if the point is outside the bounds. */
	private final int xyToIndex (float x, float y) {

		if (x < offsetX || x >= x2 || y < offsetY || y >= y2) {
			return -1;
		}

		return ((int)((x - offsetX) * cellSizeInv)) + cellsXY * ((int)((y - offsetY) * cellSizeInv));
	}
}
