
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

public class GridMap2DField {

	private static final int GRIDMAP_NULL = -1;

	private final GridMapEntry getEntry;
	
	private final GridMapOptimized2D[] gridMaps;

	private int[] eidToGridMap;

	private final int numGridMapsXY;
	private final int numCellsXY;
	private final int cellSizeXY;
	private final int initialBucketCapacity;

	private final int gridMapSizeXY;
	private final float gridMapSizeXYinv;

	private final float boundXY;

	public GridMap2DField (int numGridMapsXY, int numCellsXY, int cellSizeXY, int initialBucketCapacity) {

		getEntry = new GridMapEntry();
		
		gridMaps = new GridMapOptimized2D[numGridMapsXY * numGridMapsXY];
		eidToGridMap = new int[1024];

		Arrays.fill(eidToGridMap, GRIDMAP_NULL);

		this.numGridMapsXY = numGridMapsXY;
		this.numCellsXY = numCellsXY;
		this.cellSizeXY = cellSizeXY;
		this.initialBucketCapacity = initialBucketCapacity;

		gridMapSizeXY = numCellsXY * cellSizeXY;
		gridMapSizeXYinv = 1f / (float)gridMapSizeXY;

		boundXY = numGridMapsXY * gridMapSizeXY;
	}

	/** Clears all stored entities. The map is empty after this operation. */
	public void clear () {

		for (int i = 0; i < gridMaps.length; i++) {

			final GridMapOptimized2D map = gridMaps[i];

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
	public void get (float x1, float y1, float x2, float y2, int gf, GridMapQuery result) {

		// Return nothing if the query region is out of bounds
		if (x2 < 0 || y2 < 0 || x1 >= boundXY || y1 >= boundXY) {
			return;
		}

		final int mapX1 = Math.max((int)(x1 * gridMapSizeXYinv) - 1, 0);
		final int mapY1 = Math.max((int)(y1 * gridMapSizeXYinv) - 1, 0);
		final int mapX2 = Math.min((int)(x2 * gridMapSizeXYinv) + 1, numGridMapsXY - 1);
		final int mapY2 = Math.min((int)(y2 * gridMapSizeXYinv) + 1, numGridMapsXY - 1);

		for (int mapY = mapY1; mapY <= mapY2; mapY++) {

			final int mapX_start = mapY * numGridMapsXY + mapX1;
			final int mapX_end = mapY * numGridMapsXY + mapX2;

			for (int mapX = mapX_start; mapX <= mapX_end; mapX++) {

				GridMapOptimized2D gridMap = gridMaps[mapX];

				if (gridMap == null) {
					continue;
				}

				gridMap.get(x1, y1, x2, y2, gf, result);
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

		final int gridMapIndex = xyToIndex(x, y);

		if (gridMapIndex == GRIDMAP_NULL) {
			return false;
		}

		final GridMapOptimized2D gridMap;

		if (gridMaps[gridMapIndex] != null) {
			gridMap = gridMaps[gridMapIndex];
		} else {
			gridMaps[gridMapIndex] = gridMap = new GridMapOptimized2D(numCellsXY, cellSizeXY, initialBucketCapacity);
			
			final float gridMapXOffset = (gridMapIndex % numGridMapsXY) * gridMapSizeXY;
			final float gridMapYOffset = (gridMapIndex / numGridMapsXY) * gridMapSizeXY;
			
			gridMap.setOffset(gridMapXOffset, gridMapYOffset);
		}

		if (!gridMap.put(e, x, y, r, gf)) {
			return false;
		}
		
		createEidToGridMapLink(e, gridMapIndex);
		
		return true;
	}

	/** Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found. */
	public final boolean remove (int e) {
		
		if(e >= eidToGridMap.length) {
			return false;
		}
		
		final int gridMapIndex = eidToGridMap[e];
		
		if(gridMapIndex == GRIDMAP_NULL) {
			return false;
		}
		
		eidToGridMap[e] = GRIDMAP_NULL;
		
		if(gridMaps[gridMapIndex] == null) {
			return false;
		}
		
		return gridMaps[gridMapIndex].remove(e);
	}

	/** Updates the position of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity does not belong to this map anymore. */
	public boolean update (int e, float x, float y) {

		if(e >= eidToGridMap.length) {
			return false;
		}
		
		final int gridMapIndex = eidToGridMap[e];
		
		if(gridMapIndex == GRIDMAP_NULL) {
			return false;
		}
		
		GridMapOptimized2D gridMap = gridMaps[gridMapIndex];
		
		if(gridMap == null) {
			return false;
		}
		
		// If updating fails we try to
		if(!gridMap.update(e, x, y)) {
			
			gridMap.get(e, getEntry);
			gridMap.remove(e);
			
			return put(e, x, y, getEntry.r, getEntry.gf);
			
		} else {
			return true;
		}
	}

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

		if (x < 0 || x >= boundXY || y < 0 || y >= boundXY) {
			return GRIDMAP_NULL;
		}

		return ((int)(x * gridMapSizeXYinv)) + numGridMapsXY * ((int)(y * gridMapSizeXYinv));
	}
}
