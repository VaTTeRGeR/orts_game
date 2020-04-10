
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

import com.artemis.utils.IntBag;

/** GridMap2D sorts entities (id, x, y, [radius, flag]) into buckets, allowing for very fast tagged sub-region queries like:
 * <ul><li>Get all entity-ids inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all entity-ids and their collision-data (x, y, radius) inside Rectangle(x1,y1,x2,y2)</li>
 * <li>Get all entity-ids inside Rectangle(x1,y1,x2,y2) with flag 0x64</li></ul>
 * <p>
 * The entities can be tagged with a bit-flag and filtered by this bit-flag to only return specific types of entities.
 * @author VaTTeRGeR */
public class GridMapOptimized2D {

	/** marks the buckets storage space as not yet allocated. */
	private static final int UNALLOCATED = -1;

	/** bucket-index -> pointer into shared storage memory (*Mem). */
	private int[] pointerMap;
	/** bucket-index -> bucket-size. */
	private int[] sizeMap;
	/** bucket-index -> bucket-capacity. */
	private int[] capacityMap;

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

	/** the amount of shared memory that is currently used. */
	private int memSize;
	/** the capacity of the shared memory. */
	private int memCapacity;

	/** number of cells in x and y direction. */
	private int cellsXY;

	/** x and y cell-dimensions in world coordinates. */
	private int cellSize;

	/** inverse of x and y cell-dimensions in world coordinates. */
	private float cellSizeInv;

	/** newly created buckets will have this capacity. */
	private int initialBucketSize;

	/** x-offset in world coordinates. */
	private float offsetX;
	/** y-offset in world coordinates. */
	private float offsetY;

	/** pre-calculated upper bounds of this GridMap (->rectangle) */
	private float x2, y2;

	/** Constructs a GridMap covering a rectangle with a width and height of (cellsXY * cellSize) units.
	 * @param cellsXY The number of cells/buckets in X and Y direction.
	 * @param cellSize The size of each bucket/cell.
	 * @param maxEntities The expected maximum number of entities in this GridMap. Ideally slightly larger than the expected entity
	 *           count to avoid resizing.
	 * @param initialBucketSize The default bucket-size. Set this to the expected number of entities per bucket ideally. */
	public GridMapOptimized2D (int cellsXY, int cellSize, int maxEntities, int initialBucketSize) {

		if (cellsXY < 1 || cellsXY > Short.MAX_VALUE) {
			throw new IllegalArgumentException("cellsXY out of range [1," + Short.MAX_VALUE + "]:" + cellsXY);
		}

		if (cellSize < 1 || cellSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("cellsSize out of range [1," + Integer.MAX_VALUE + "]:" + cellSize);
		}

		if (maxEntities < 1) {
			throw new IllegalArgumentException("maxEntities out of range [1," + Integer.MAX_VALUE + "]:" + maxEntities);
		}

		this.cellsXY = cellsXY;
		
		this.cellSize = cellSize;
		this.cellSizeInv = 1f / cellSize;
		
		this.initialBucketSize = initialBucketSize;

		setOffset(0f, 0f);

		final int totalCells = cellsXY * cellsXY;

		// Buckets
		pointerMap = new int[totalCells];
		sizeMap = new int[totalCells];
		capacityMap = new int[totalCells];

		// Contents of the buckets
		eidMem = new int[maxEntities];
		flagMem = new int[maxEntities];
		xMem = new float[maxEntities];
		yMem = new float[maxEntities];
		rMem = new float[maxEntities];

		// buckets with no active storage get initialized as -1 and have zero size and capacity.
		Arrays.fill(pointerMap, UNALLOCATED);
		
		memSize = 0;
		memCapacity = maxEntities;
	}

	/** Clears all stored entities. */
	public void clear () {
		Arrays.fill(sizeMap, 0);
	}

	/**
	 * Fills the provided {@link IntBag} with entity-ids that fall inside the specified rectangle [x1,y1,x2,y2].
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param bag The {@link IntBag} that will contain the found entity-ids afterwards.
	 */
	public void getIdOnly (float x1, float y1, float x2, float y2, int gf, IntBag bag) {

		// Return nothing if the query region is out of bounds
		if (x2 < offsetX || y2 < offsetY || x1 > this.x2 || y1 > this.y2) {
			return;
		}

		x1 = Math.max(x1, offsetX);
		y1 = Math.max(y1, offsetY);

		x2 = Math.min(x2, this.x2 - cellSize * 0.5f);
		y2 = Math.min(y2, this.y2 - cellSize * 0.5f);

		final int bucketX1 = (int)(x1 * cellSizeInv);
		final int bucketY1 = (int)(y1 * cellSizeInv);

		final int bucketX2 = (int)(x2 * cellSizeInv);
		final int bucketY2 = (int)(y2 * cellSizeInv);

		for (int y = bucketY1; y <= bucketY2; y++) {
			for (int x = bucketX1; x <= bucketX2; x++) {

				final int bucket = x + y * cellsXY;
				final int pointer_start = pointerMap[bucket];
				final int pointer_end = pointer_start + sizeMap[bucket];

				for (int p = pointer_start; p < pointer_end; p++) {
					if (GridMapUtil.isContaining(flagMem[p], gf)) {
						bag.add(eidMem[p]);
					}
				}
			}
		}
	}

	/**
	 * Inserts the entity with the specified data into this {@link GridMapOptimized2D}.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @param e The id of the entity.
	 * @param gf The flags assigned to the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds.
	 */
	public boolean put (float x, float y, float r, int e, int gf) {

		final int bucketIndex = xyToIndex(x, y);

		if (bucketIndex == -1) return false;

		final int sharedMemPointer;

		if (pointerMap[bucketIndex] == UNALLOCATED) {
			sharedMemPointer = allocateNewBucket(bucketIndex, initialBucketSize);
		} else {
			sharedMemPointer = pointerMap[bucketIndex];
		}

		// Get and increment size! DO NOT USE sizeMem UNTIL END OF FUNCTION!
		final int bucketSize = sizeMap[bucketIndex]++;
		final int bucketCapacity = capacityMap[bucketIndex];

		// If the bucket is full its capacity gets doubled.
		if (bucketSize == bucketCapacity) {
			growBucket(bucketIndex, bucketCapacity);
		}

		final int sharedMemCellPointer = sharedMemPointer + bucketSize;

		eidMem[sharedMemCellPointer] = e;
		flagMem[sharedMemCellPointer] = gf;

		xMem[sharedMemCellPointer] = x;
		yMem[sharedMemCellPointer] = y;
		rMem[sharedMemCellPointer] = r;

		return true;
	}

	private int allocateNewBucket (int bucketIndex, int bucketSize) {

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

	/** Enlarges a single bucket by copying succeeding bucket-contents into a later portion of their backing arrays.<br>
	 * This may also cause a call to {@link #growSharedMemory} that doubles the backing arrays.
	 * @param bucketIndex
	 * @param growAmount */
	private void growBucket (int bucketIndex, int growAmount) {

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
	}

	/** Enlarges the shared memory space by creating a new set of backing arrays and copying over the old content.
	 * @param growAmount The amount of additional slots needed. */
	private void growSharedMemory (int growAmount) {

		memCapacity += growAmount;

		eidMem = Arrays.copyOf(eidMem, memCapacity);
		flagMem = Arrays.copyOf(flagMem, memCapacity);
		xMem = Arrays.copyOf(xMem, memCapacity);
		yMem = Arrays.copyOf(yMem, memCapacity);
		rMem = Arrays.copyOf(rMem, memCapacity);
	}

	/** Calculates the free space inside the shared storage, measured as the number of unused slots after the last bucket.
	 * @return The amount of free space in the shared arrays. */
	private int getFreeMemory () {
		return memCapacity - memSize;
	}

	/** Sets the world offset of this GridMap.
	 * @param offsetX The offset in x-direction.
	 * @param offsetY The offset in y-direction. */
	public void setOffset (float offsetX, float offsetY) {

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
	private int xyToIndex (float x, float y) {

		if (x < offsetX || x >= x2 || y < offsetY || y >= y2) {
			return -1;
		}

		return xyToIndexUnchecked(x, y);
	}

	/** Calculates the index of the requested bucket from the supplied world coordinates without bounds checks.
	 * <p>
	 * Pseudo-Formula: i = width * y / cellSize + x / cellSize
	 * @param x The x coordinate in world coordinates.
	 * @param y The y coordinate in world coordinates.
	 * @return The index inside the {@link #pointerMap} array. */
	private int xyToIndexUnchecked (float x, float y) {
		return ((int)(x * cellSizeInv)) + cellsXY * ((int)(y * cellSizeInv));
	}
}
