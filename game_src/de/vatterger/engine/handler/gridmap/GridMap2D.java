
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

/** GridMap2D sorts entities (id, x, y, [radius, flag]) into equally distributed same sized buckets, allowing for fast tagged
 * sub-region queries as well as update and removal of entries:
 * <ul>
 * <li>Get all entity-ids inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all collision-data (x, y, radius) inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all entity-ids and collision data inside Rectangle(x1,y1,x2,y2) with bit-flag 0x64 set</li>
 * </ul>
 * <p>
 * Entities can be tagged with a bit-flag and filtered by this bit-flag when searching the map.
 * <p>
 * Put(), update() and remove() are O(1) if the buckets are appropriately sized. Get is O(N*M) with respect to the number of
 * buckets N*M covered by the search area, therefore smaller buckets (cellSize) increase the cost of get(). Therefore it is
 * important to choose initial bucket size and bucket dimensions correctly for you workload.
 * <p>
 * <b>Remember to set the cellSize at least as large as the largest possible radius of any inserted entity, otherwise large
 * entities will not be returned even though they intersect the search area (false negatives).</b>
 * @author VaTTeRGeR */
public class GridMap2D {

	/** marks the buckets storage space as not yet allocated. */
	private static final int UNALLOCATED = -1;
	/** marks the bucket-pointer as null. */
	private static final int BUCKET_NULL = -1;
	/** marks the pointer into shared memory as null. */
	private static final int POINTER_NULL = -1;

	/** the recursive memory stealing routine will look at this many buckets before giving up. */
	private static final int BUCKET_STEAL_RECURSIVE_DEPTH = 10;

	/** bucket-index -> pointer into shared storage memory (*Mem). */
	private final int[] pointerMap;
	/** bucket-index -> bucket-size. */
	private final int[] sizeMap;
	/** bucket-index -> bucket-capacity. */
	private final int[] capacityMap;

	/** shared storage: entity ID. */
	private int[] eidMem;
	/** shared storage: entity Flags. */
	private int[] gfMem;

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

	/** x and y bucket-dimensions in world coordinates. */
	private final int cellSize;

	/** inverse of x and y bucket-dimensions in world coordinates. */
	private final float cellSizeInv;

	/** newly created buckets will have this capacity. */
	private final int initialBucketSize;

	/** pre-calculated lower left bounds of this GridMap (->rectangle) */
	private final float borderX1, borderY1;

	/** pre-calculated upper right bounds of this GridMap (->rectangle) */
	private final float borderX2, borderY2;

	/** Constructs a GridMap covering a rectangle with a width and height of (cellsXY * cellSize) units.
	 * <p>
	 * <b>Remember to set the cellSize at least as large as the largest possible radius of any inserted entity, otherwise large
	 * entities will not be returned even though they intersect the search area (false negatives).</b>
	 * @param numCellsXY The number of cells/buckets in X and Y direction.
	 * @param cellSizeXY The dimensions of each bucket.
	 * @param initialBucketCapacity Each bucket will have this initial capacity. Higher values mean less bucket resizing (first
	 *           round of put) but more memory usage and less memory locality (slower get). Remember to leave a bit of headroom so
	 *           that buckets can steal slots from each other.
	 * @param offsetX The origin coordinate of the map in x-direction.
	 * @param offsetY The origin coordinate of the map in y-direction. */
	public GridMap2D (int numCellsXY, int cellSizeXY, int initialBucketCapacity, float offsetX, float offsetY) {

		if (numCellsXY < 1 || numCellsXY > Short.MAX_VALUE) {
			throw new IllegalArgumentException("cellsXY out of range [1," + Short.MAX_VALUE + "]:" + numCellsXY);
		}

		if (cellSizeXY < 1) {
			throw new IllegalArgumentException("cellsSize out of range [1," + Integer.MAX_VALUE + "]:" + cellSizeXY);
		}

		if (initialBucketCapacity < 1) {
			throw new IllegalArgumentException("initialBucketSize out of range [1," + Integer.MAX_VALUE + "]:" + cellSizeXY);
		}

		// calculate bounds and helper values.
		this.cellsXY = numCellsXY;

		this.cellSize = cellSizeXY;
		this.cellSizeInv = 1f / cellSizeXY;

		this.borderX1 = offsetX;
		this.borderY1 = offsetY;

		borderX2 = offsetX + cellSize * cellsXY;
		borderY2 = offsetY + cellSize * cellsXY;

		// setup buckets.
		this.initialBucketSize = initialBucketCapacity;

		final int totalCells = numCellsXY * numCellsXY;

		pointerMap = new int[totalCells];
		sizeMap = new int[totalCells];
		capacityMap = new int[totalCells];

		memSize = 0;
		memCapacity = initialBucketCapacity * totalCells;

		// setup contents of the buckets.
		eidMem = new int[memCapacity];
		gfMem = new int[memCapacity];
		xMem = new float[memCapacity];
		yMem = new float[memCapacity];
		rMem = new float[memCapacity];

		eidToBucketMem = new int[memCapacity];

		// buckets with no active storage get initialized as UNALLOCATED and have zero size and capacity.
		Arrays.fill(pointerMap, UNALLOCATED);
		// no entity has been inserted yet so the bucket pointers are all initialized as BUCKET_NULL
		Arrays.fill(eidToBucketMem, BUCKET_NULL);

		for (int i = 0; i < totalCells; i++) {
			allocateNewBucket(i, initialBucketCapacity);
		}
	}

	/** Clears all stored entities. The map is empty after this operation. */
	public void clear () {
		Arrays.fill(sizeMap, 0);
	}

	/** Fills the provided {@link GridMapEntry} with the data of the specified entity.
	 * @param e The entity-id whose data shall be collected.
	 * @param result The {@link GridMapEntry} that will contain the collected data.
	 * @return True if the entity was found and the data collected, otherwise false. The {@link GridMapEntry} will be unmodified if
	 *         the method returns false. */
	public boolean get (int e, GridMapEntry result) {

		if (e >= eidToBucketMem.length) {
			return false;
		}

		final int bucketIndex = eidToBucketMem[e];

		// The entry has not been inserted yet or was already removed.
		if (bucketIndex == BUCKET_NULL) {
			return false;
		}

		int pointerEntity = getPointerToEntity(e, bucketIndex);

		// The entry was not found in its bucket
		if (pointerEntity == POINTER_NULL) {
			return false;
		}

		result.e = e;
		result.gf = gfMem[pointerEntity];
		result.x = xMem[pointerEntity];
		result.y = yMem[pointerEntity];
		result.r = rMem[pointerEntity];

		return true;
	}

	/** Fills the provided containers with nearby entity-ids and their collision-data.
	 * @param x X-Coordinate of the point.
	 * @param y Y-Coordinate of the point.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. public void get (float x, float y,
	 *           GridMapQuery result) { get(x, y, x, y, 0, result); }
	 * 
	 *           /** Fills the provided containers with nearby entity-ids and their collision-data if they have the specified
	 *           bitflags set.
	 * @param x X-Coordinate of the point.
	 * @param y Y-Coordinate of the point.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. */
	public void get (float x, float y, int gf, GridMapQuery result) {
		get(x, y, x, y, gf, result);
	}

	/** Fills the provided containers with entity-ids and their collision-data if they fall inside the specified rectangle
	 * [x1,y1,x2,y2].
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. */
	public void get (float x1, float y1, float x2, float y2, GridMapQuery result) {
		get(x1, y1, x2, y2, 0, result);
	}

	/** Fills the provided containers with entity-ids and their collision-data if they fall inside the specified rectangle
	 * [x1,y1,x2,y2] and have the specified bitflags set.
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. */
	public boolean get (float x1, float y1, float x2, float y2, int gf, GridMapQuery result) {

		// Return nothing if the query region is grossly out of bounds
		if (x2 <= borderX1 - cellSize || y2 <= borderY1 - cellSize || x1 >= borderX2 + cellSize || y1 >= borderY2 + cellSize) {
			return false;
		}

		x1 -= borderX1;
		x2 -= borderX1;

		y1 -= borderY1;
		y2 -= borderY1;

		final int bucketX1 = Math.max((int)((x1 - cellSize * 0.5f) * cellSizeInv) /*- 1*/, 0);
		final int bucketY1 = Math.max((int)((y1 - cellSize * 0.5f) * cellSizeInv) /*- 1*/, 0);
		final int bucketX2 = Math.min((int)((x2 + cellSize * 0.5f) * cellSizeInv) /* + 1 */, cellsXY - 1);
		final int bucketY2 = Math.min((int)((y2 + cellSize * 0.5f) * cellSizeInv) /* + 1 */, cellsXY - 1);

		final boolean gridFlagPresent = gf != 0;

		if (result.idArray != null && result.colArray == null) {

			for (int bucketY = bucketY1; bucketY <= bucketY2; bucketY++) {

				final int bucketX_start = bucketY * cellsXY + bucketX1;
				final int bucketX_end = bucketY * cellsXY + bucketX2;

				for (int bucketX = bucketX_start; bucketX <= bucketX_end; bucketX++) {

					final int pointer_start = pointerMap[bucketX];
					final int pointer_end = pointer_start + sizeMap[bucketX];

					for (int p = pointer_start; p < pointer_end; p++) {

						if (!gridFlagPresent || GridMapFlagUtil.isContaining(gfMem[p], gf)) {
							result.put(eidMem[p]);
						}
					}
				}
			}

		} else if (result.colArray != null && result.idArray == null) {

			for (int bucketY = bucketY1; bucketY <= bucketY2; bucketY++) {

				final int bucketX_start = bucketY * cellsXY + bucketX1;
				final int bucketX_end = bucketY * cellsXY + bucketX2;

				for (int bucketX = bucketX_start; bucketX <= bucketX_end; bucketX++) {

					final int pointer_start = pointerMap[bucketX];
					final int pointer_end = pointer_start + sizeMap[bucketX];

					for (int p = pointer_start; p < pointer_end; p++) {

						if (!gridFlagPresent || GridMapFlagUtil.isContaining(gfMem[p], gf)) {
							result.put(xMem[p], yMem[p], rMem[p]);
						}
					}
				}
			}
		} else if (result.idArray != null && result.colArray != null) {

			for (int bucketY = bucketY1; bucketY <= bucketY2; bucketY++) {

				final int bucketX_start = bucketY * cellsXY + bucketX1;
				final int bucketX_end = bucketY * cellsXY + bucketX2;

				for (int bucketX = bucketX_start; bucketX <= bucketX_end; bucketX++) {

					final int pointer_start = pointerMap[bucketX];
					final int pointer_end = pointer_start + sizeMap[bucketX];

					for (int p = pointer_start; p < pointer_end; p++) {

						if (!gridFlagPresent || GridMapFlagUtil.isContaining(gfMem[p], gf)) {
							result.put(eidMem[p], xMem[p], yMem[p], rMem[p]);
						}
					}
				}
			}
		}

		return true;
	}

	/** Inserts the entity with the specified data into this {@link GridMap2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y) {
		return put(e, x, y, 0f, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y, float r) {
		return put(e, x, y, r, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2D}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @param gf The flags assigned to the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	public boolean put (int e, float x, float y, float r, int gf) {

		final int bucketIndex = xyToIndex(x, y);

		if (bucketIndex == BUCKET_NULL) {
			return false;
		}

		final int sharedMemPointer;

		if (pointerMap[bucketIndex] != UNALLOCATED) {
			sharedMemPointer = pointerMap[bucketIndex];
		} else {
			sharedMemPointer = allocateNewBucket(bucketIndex, initialBucketSize);
		}

		// If the bucket is full we first try to steal memory from adjacent cells
		// and only if not successful we will resort to expensive full memory shifting.
		if (sizeMap[bucketIndex] == capacityMap[bucketIndex]) {
			if (!tryStealMemory(bucketIndex, 1, BUCKET_STEAL_RECURSIVE_DEPTH)) {
				growBucket(bucketIndex, capacityMap[bucketIndex]);
			}
		}

		final int sharedMemCellPointer = sharedMemPointer + sizeMap[bucketIndex];

		sizeMap[bucketIndex]++;

		eidMem[sharedMemCellPointer] = e;
		gfMem[sharedMemCellPointer] = gf;

		xMem[sharedMemCellPointer] = x;
		yMem[sharedMemCellPointer] = y;
		rMem[sharedMemCellPointer] = r;

		final int eidToBucketMemLen = eidToBucketMem.length;

		if (eidToBucketMemLen <= e) {
			eidToBucketMem = Arrays.copyOf(eidToBucketMem, Math.max(e + 1, eidToBucketMemLen * 2));
			Arrays.fill(eidToBucketMem, eidToBucketMemLen, eidToBucketMem.length, BUCKET_NULL);
		}

		eidToBucketMem[e] = bucketIndex;

		return true;
	}

	/** Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found. */
	public final boolean remove (int e) {

		if (e >= eidToBucketMem.length) {
			return false;
		}

		final int bucketIndex = eidToBucketMem[e];

		// The entry has not been inserted yet or was already removed.
		if (bucketIndex == BUCKET_NULL) {
			return false;
		}

		eidToBucketMem[e] = BUCKET_NULL;

		int pointerEntity = getPointerToEntity(e, bucketIndex);

		// The entry was not found in its bucket
		if (pointerEntity == POINTER_NULL) {
			return false;
		}

		final int pointerLastEntry = pointerMap[bucketIndex] + sizeMap[bucketIndex] - 1;

		// We swap e with the last entry in the bucket if e is not the last entry.
		if (pointerEntity < pointerLastEntry) {
			eidMem[pointerEntity] = eidMem[pointerLastEntry];
			gfMem[pointerEntity] = gfMem[pointerLastEntry];
			xMem[pointerEntity] = xMem[pointerLastEntry];
			yMem[pointerEntity] = yMem[pointerLastEntry];
			rMem[pointerEntity] = rMem[pointerLastEntry];
		}

		sizeMap[bucketIndex]--;

		return true;
	}

	/** Allows you to judge how much free space is present in the gridmap. This method is O(N*N) with N = cellsXY.
	 * @return An variable length array of ints that encodes how often a certain number of free slots are present. */
	public int[] getFreeSpaceDistribution () {

		int[] sizeArray = new int[1];

		for (int i = 0; i < sizeMap.length && i < capacityMap.length; i++) {

			final int slot = capacityMap[i] - sizeMap[i];

			if (slot >= sizeArray.length) {
				sizeArray = Arrays.copyOf(sizeArray, slot + 1);
			}

			sizeArray[slot]++;
		}

		return sizeArray;
	}

	/** Updates the position of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity does not belong to this map anymore. */
	public boolean update (int e, float x, float y) {

		if (eidToBucketMem.length <= e) {
			return false;
		}

		final int bucketIndex = eidToBucketMem[e];

		if (bucketIndex == BUCKET_NULL) {
			return false;
		}

		final int pointer = getPointerToEntity(e, bucketIndex);

		if (pointer == POINTER_NULL) {
			return false;
		}

		final int newBucketIndex = xyToIndex(x, y);

		if (newBucketIndex == BUCKET_NULL) {
			return false;
		}

		// If true the object stayed in it's bucket else it gets rehomed.
		// If it leaves this gridmap it returns false because put fails.
		if (bucketIndex == newBucketIndex) {

			xMem[pointer] = x;
			yMem[pointer] = y;

			return true;

		} else {

			final int gf = gfMem[pointer];
			final float r = rMem[pointer];

			return remove(e) && put(e, x, y, r, gf);
		}
	}

	/** Returns the absolute index of the entity.
	 * @param e The id of the entity to search for.
	 * @param bucketIndex The bucket to search in. Calculate this with {@link #xyToIndex(float, float)}
	 * @return The index to the entities data inside the shared memory or -1 if not found. */
	private final int getPointerToEntity (int e, int bucketIndex) {

		final int pointerBucketBegin = pointerMap[bucketIndex];
		final int pointerBucketEnd = pointerBucketBegin + sizeMap[bucketIndex] - 1;

		// Frequently removed and inserted entries end up towards the end of the bucket.
		for (int i = pointerBucketEnd; i >= pointerBucketBegin; i--) {

			if (eidMem[i] == e) {
				return i;
			}
		}

		return POINTER_NULL;
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

		// Stop-condition for recursive search.
		if (recursiveDepth <= 0) return false;

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
			System.arraycopy(gfMem, nextBucketPointer, gfMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(xMem, nextBucketPointer, xMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(yMem, nextBucketPointer, yMem, nextBucketPointerNew, nextBucketRemainingCapacity);
			System.arraycopy(rMem, nextBucketPointer, rMem, nextBucketPointerNew, nextBucketRemainingCapacity);

			return true;
		}

		return false;
	}

	/** Enlarges a single bucket by either stealing from succeeding buckets (fast) or enlarging/shifting the whole shared memory
	 * (slow). Give each bucket a bit of unused capacity to keep the cost of this method down.<br>
	 * This may also cause a call to {@link #growSharedMemory} that doubles the backing arrays to provide the needed space.
	 * @param bucketIndex
	 * @param growAmount */
	private final void growBucket (int bucketIndex, int growAmount) {

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
			System.arraycopy(gfMem, succeedingBucketPointer, gfMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(xMem, succeedingBucketPointer, xMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(yMem, succeedingBucketPointer, yMem, succeedingBucketPointerNew, moveAmount);
			System.arraycopy(rMem, succeedingBucketPointer, rMem, succeedingBucketPointerNew, moveAmount);
		}
	}

	/** Enlarges the shared memory space by creating a new set of backing arrays and copying over the old content.
	 * @param growAmount The amount of additional slots needed. */
	private final void growSharedMemory (int growAmount) {

		memCapacity += growAmount;

		eidMem = Arrays.copyOf(eidMem, memCapacity);
		gfMem = Arrays.copyOf(gfMem, memCapacity);
		xMem = Arrays.copyOf(xMem, memCapacity);
		yMem = Arrays.copyOf(yMem, memCapacity);
		rMem = Arrays.copyOf(rMem, memCapacity);
	}

	/** Calculates the free space inside the shared storage, measured as the number of unused slots after the last bucket.
	 * @return The amount of free space in the shared arrays. */
	private final int getFreeMemory () {
		return memCapacity - memSize;
	}

	/** Calculates the index of the requested bucket from the supplied world coordinates.
	 * <p>
	 * Pseudo-Formula: i = width * y / cellSize + x / cellSize
	 * @param x The x coordinate in world coordinates.
	 * @param y The y coordinate in world coordinates.
	 * @return The index inside the {@link #pointerMap} array or -1 if the point is outside the bounds. */
	private final int xyToIndex (float x, float y) {

		if (x < borderX1 || x >= borderX2 || y < borderY1 || y >= borderY2) {
			return BUCKET_NULL;
		}

		x -= borderX1;
		y -= borderY1;

		return ((int)(x * cellSizeInv)) + cellsXY * ((int)(y * cellSizeInv));
	}
}
