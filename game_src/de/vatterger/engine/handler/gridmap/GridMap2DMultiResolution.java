
package de.vatterger.engine.handler.gridmap;

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
public class GridMap2DMultiResolution implements GridMap2D {

	private final GridMap2DField[] gridMap2DFields;

	private final float[] gridMap2DFieldAreas;

	public GridMap2DMultiResolution (int[] cellSizeXYScales, int numGridMapsXY, int numCellsXY, int cellSizeXY,
		int initialBucketCapacity, float offsetX, float offsetY) {

		if (cellSizeXYScales == null) {
			throw new IllegalArgumentException("cellSizeXYScales is null.");
		}

		if (cellSizeXYScales.length < 1) {
			throw new IllegalArgumentException(
				"cellSizeXYScales needs at least one level apart from the default one. Use GridMap2DField if you do not need multiple levels.");
		}

		gridMap2DFields = new GridMap2DField[cellSizeXYScales.length + 1];
		gridMap2DFieldAreas = new float[cellSizeXYScales.length + 1];

		// The area of one GridMap-cell inside the default (lowest level => scale=1) gridMap2DField
		gridMap2DFieldAreas[0] = cellSizeXY * cellSizeXY;

		gridMap2DFields[0] = new GridMap2DField(numGridMapsXY, numCellsXY, cellSizeXY, initialBucketCapacity, offsetX, offsetY);

		for (int i = 0; i < cellSizeXYScales.length; i++) {

			final int cellScale = cellSizeXYScales[i];
			final int cellScaleSquared = cellScale * cellScale;

			if (cellScale < 2) {
				throw new IllegalArgumentException("cellSizeXYScales[" + i + "] is less then 2: " + cellScale);
			}

			if (numCellsXY % cellScale != 0) {
				throw new IllegalArgumentException(
					"cellSizeXYScales[" + i + "] = " + cellScale + " is not a divisor of: " + cellSizeXY);
			}

			final int numCellsXYScaled = numCellsXY / cellScale;
			final int cellSizeXYScaled = cellSizeXY * cellScale;
			final int initialBucketCapacityScaled = initialBucketCapacity * cellScaleSquared;

			if (numCellsXYScaled < 1) {
				throw new IllegalArgumentException(
					"cellSizeXYScales[" + i + "] = " + cellScale + " is cannot be greater then numCellsXY=" + numCellsXY);
			}

			gridMap2DFields[i + 1] = new GridMap2DField(numGridMapsXY, numCellsXYScaled, cellSizeXYScaled,
				initialBucketCapacityScaled, offsetX, offsetY);
			gridMap2DFieldAreas[i + 1] = cellSizeXYScaled * cellSizeXYScaled;
		}
	}

	/** Clears all stored entities. */
	@Override
	public void clear () {
		for (GridMap2DField gridMapField : gridMap2DFields) {
			gridMapField.clear();
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

		final float searchAreaInv = 1f / ((x2 - x1) * (y2 - y1));

		int selectedGridMap = 1;

		while (selectedGridMap < gridMap2DFieldAreas.length
			&& gridMap2DFieldAreas[selectedGridMap] * searchAreaInv < (1f / (float)(4 * 4 * 4))) {
			selectedGridMap++;
		}

		gridMap2DFields[selectedGridMap - 1].get(x1, y1, x2, y2, gf, result);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds in any of the resolution
	 *         levels. */
	@Override
	public boolean put (int e, float x, float y) {
		return put(e, x, y, 0f, 0);
	}

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds in any of the resolution
	 *         levels. */
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
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds in any of the resolution
	 *         levels. */
	@Override
	public boolean put (int e, float x, float y, float r, int gf) {

		boolean success = true;

		for (GridMap2DField gridMap2DField : gridMap2DFields) {
			success &= gridMap2DField.put(e, x, y, r, gf);
		}

		if (!success) {
			remove(e);
		}

		return success;
	}

	/** Checks if entity e is contained inside this {@link GridMap2DMultiResolution}. Default resolution {@link GridMap2DField} is
	 * queried.
	 * @param e The id of the entity that should be checked.
	 * @return True if e is contained in this {@link GridMap2DMultiResolution} otherwise false. */
	@Override
	public final boolean contains (int e) {
		return gridMap2DFields[0].contains(e);
	}

	/** Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found in any of the resolution levels. */
	@Override
	public final boolean remove (int e) {

		boolean success = true;

		for (GridMap2DField gridMap2DField : gridMap2DFields) {
			success &= gridMap2DField.remove(e);
		}

		return success;
	}

	/** Updates the position of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity does not belong to this map anymore in any of the resolution levels. */
	@Override
	public boolean update (int e, float x, float y) {

		boolean success = true;

		for (GridMap2DField gridMap2DField : gridMap2DFields) {
			success &= gridMap2DField.update(e, x, y);
		}

		return success;
	}
}
