
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

/** GridMap2DField sorts entities (id, x, y, [radius, flag]) into equally distributed same sized buckets, allowing for fast tagged
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
public class GridMap2DField implements GridMap2D {

	private static final int GRIDMAP_NULL = -1;

	private final GridMapEntry flyWeightEntry;

	private final GridMap2DSimple[] gridMaps;

	private int[] eidToGridMap;

	private final int numGridMapsXY;
	private final int numCellsXY;
	private final int cellSizeXY;
	private final int initialBucketCapacity;

	private final int gridMapSizeXY;
	private final float gridMapSizeXYinv;

	private final float boundX1, boundY1;
	private final float boundX2, boundY2;

	public GridMap2DField (int numGridMapsXY, int numCellsXY, int cellSizeXY, int initialBucketCapacity, float offsetX,
		float offsetY) {

		flyWeightEntry = new GridMapEntry();

		gridMaps = new GridMap2DSimple[numGridMapsXY * numGridMapsXY];
		eidToGridMap = new int[1024];

		Arrays.fill(eidToGridMap, GRIDMAP_NULL);

		this.numGridMapsXY = numGridMapsXY;
		this.numCellsXY = numCellsXY;
		this.cellSizeXY = cellSizeXY;
		this.initialBucketCapacity = initialBucketCapacity;

		gridMapSizeXY = numCellsXY * cellSizeXY;
		gridMapSizeXYinv = 1f / (float)gridMapSizeXY;

		final float sizeXY = numGridMapsXY * gridMapSizeXY;

		boundX1 = offsetX;
		boundY1 = offsetY;

		boundX2 = offsetX + sizeXY;
		boundY2 = offsetY + sizeXY;
	}

	/** Clears all stored entities. */
	@Override
	public void clear () {

		for (int i = 0; i < gridMaps.length; i++) {

			final GridMap2DSimple map = gridMaps[i];

			if (map != null) {
				map.clear();
			}
		}
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
	@Override
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
	@Override
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
	@Override
	public void get (float x1, float y1, float x2, float y2, int gf, GridMapQuery result) {

		// Return nothing if the query region is out of bounds
		if (x2 < boundX1 || y2 < boundY1 || x1 >= boundX2 || y1 >= boundY2) {
			return;
		}

		final int mapX1 = Math.max((int)((x1 - boundX1) * gridMapSizeXYinv) - 1, 0);
		final int mapY1 = Math.max((int)((y1 - boundY1) * gridMapSizeXYinv) - 1, 0);
		final int mapX2 = Math.min((int)((x2 - boundX1) * gridMapSizeXYinv) + 1, numGridMapsXY - 1);
		final int mapY2 = Math.min((int)((y2 - boundY1) * gridMapSizeXYinv) + 1, numGridMapsXY - 1);

		for (int mapY = mapY1; mapY <= mapY2; mapY++) {

			final int mapX_start = mapY * numGridMapsXY + mapX1;
			final int mapX_end = mapY * numGridMapsXY + mapX2;

			for (int mapX = mapX_start; mapX <= mapX_end; mapX++) {

				GridMap2DSimple gridMap = gridMaps[mapX];

				if (gridMap == null) {
					continue;
				}

				gridMap.get(x1, y1, x2, y2, gf, result);
			}
		}
	}

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	@Override
	public boolean put (int e, float x, float y) {
		return put(e, x, y, 0f, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	@Override
	public boolean put (int e, float x, float y, float r) {
		return put(e, x, y, r, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @param gf The flags assigned to the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	@Override
	public boolean put (int e, float x, float y, float r, int gf) {

		final int gridMapIndex = xyToIndex(x, y);

		if (gridMapIndex == GRIDMAP_NULL) {
			return false;
		}

		final GridMap2DSimple gridMap;

		if (gridMaps[gridMapIndex] != null) {
			gridMap = gridMaps[gridMapIndex];

		} else {
			final float gridMapXOffset = boundX1 + (gridMapIndex % numGridMapsXY) * gridMapSizeXY;
			final float gridMapYOffset = boundY1 + (gridMapIndex / numGridMapsXY) * gridMapSizeXY;

			gridMaps[gridMapIndex] = gridMap = new GridMap2DSimple(numCellsXY, cellSizeXY, initialBucketCapacity, gridMapXOffset,
				gridMapYOffset);
		}

		if (!gridMap.put(e, x, y, r, gf)) {
			return false;
		}

		createEidToGridMapLink(e, gridMapIndex);

		return true;
	}

	/** Checks if entity e is contained inside this {@link GridMap2DField}.
	 * @param e The id of the entity that should be checked.
	 * @return True if e is contained in this {@link GridMap2DField} otherwise false. */
	@Override
	public final boolean contains (int e) {

		if (e >= eidToGridMap.length) {
			return false;
		}

		return eidToGridMap[e] != GRIDMAP_NULL;
	}

	/** Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found. */
	@Override
	public final boolean remove (int e) {

		if (e >= eidToGridMap.length) {
			return false;
		}

		final int gridMapIndex = eidToGridMap[e];

		if (gridMapIndex == GRIDMAP_NULL) {
			return false;
		}

		eidToGridMap[e] = GRIDMAP_NULL;

		if (gridMaps[gridMapIndex] == null) {
			return false;
		}

		return gridMaps[gridMapIndex].remove(e);
	}

	/** Updates the position of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity does not belong to this map anymore. */
	@Override
	public boolean update (int e, float x, float y) {

		if (e >= eidToGridMap.length) {
			return false;
		}

		final int gridMapIndex = eidToGridMap[e];

		if (gridMapIndex == GRIDMAP_NULL) {
			return false;
		}

		GridMap2DSimple gridMap = gridMaps[gridMapIndex];

		if (gridMap == null) {
			return false;
		}

		// If updating fails the entity has gone out of bounds for the GridMap it was in.
		// We grab its data, remove it from its current map and insert it into the correct GridMap.
		if (!gridMap.update(e, x, y)) {

			gridMap.get(e, flyWeightEntry);
			gridMap.remove(e);

			return put(e, x, y, flyWeightEntry.r, flyWeightEntry.gf);

		} else {
			return true;
		}
	}

	/** Sets the link and resizes the backing array if necessary.
	 * @param e The entity id that resides in the {@link GridMap2DSimple} indexed by gridMapIndex.
	 * @param gridMapIndex The index of the {@link GridMap2DSimple} inside the backing array. */
	private void createEidToGridMapLink (int e, int gridMapIndex) {

		final int eidToGridMapLength = eidToGridMap.length;

		if (eidToGridMapLength <= e) {
			eidToGridMap = Arrays.copyOf(eidToGridMap, Math.max(e + 1, eidToGridMapLength * 2));
			Arrays.fill(eidToGridMap, eidToGridMapLength, eidToGridMap.length, GRIDMAP_NULL);
		}

		eidToGridMap[e] = gridMapIndex;
	}

	/** Calculates the index of the requested bucket from the supplied world coordinates.
	 * <p>
	 * Pseudo-Formula: i = width * y / cellSize + x / cellSize
	 * @param x The x coordinate in world coordinates.
	 * @param y The y coordinate in world coordinates.
	 * @return The index inside the {@link #pointerMap} array or -1 if the point is outside the bounds. */
	private final int xyToIndex (float x, float y) {

		if (x < boundX1 || x >= boundX2 || y < boundY1 || y >= boundY2) {
			return GRIDMAP_NULL;
		}

		x -= boundX1;
		y -= boundY1;

		return ((int)(x * gridMapSizeXYinv)) + numGridMapsXY * ((int)(y * gridMapSizeXYinv));
	}
}
