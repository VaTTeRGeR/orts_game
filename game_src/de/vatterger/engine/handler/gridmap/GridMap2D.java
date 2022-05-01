
package de.vatterger.engine.handler.gridmap;

public interface GridMap2D {

	/** Clears all stored entities. The map is empty after this operation. */
	void clear ();

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
	void get (float x, float y, int gf, GridMapQuery result);

	/** Fills the provided containers with entity-ids and their collision-data if they fall inside the specified rectangle
	 * [x1,y1,x2,y2].
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. */
	void get (float x1, float y1, float x2, float y2, GridMapQuery result);

	/** Fills the provided containers with entity-ids and their collision-data if they fall inside the specified rectangle
	 * [x1,y1,x2,y2] and have the specified bitflags set.
	 * @param x1 X-Coordinate of the lower left corner.
	 * @param y1 Y-Coordinate of the lower left corner.
	 * @param x2 X-Coordinate of the upper right corner.
	 * @param y2 Y-Coordinate of the upper right corner.
	 * @param gf Only entities with these bit-flags set will be returned. Use zero if you want to ignore bit-flags.
	 * @param result The {@link GridMapQuery} object that gets filled with the collected data. */
	void get (float x1, float y1, float x2, float y2, int gf, GridMapQuery result);

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	boolean put (int e, float x, float y);

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @return True if it was inserted. False if it couldn't be inserted because it was out of bounds. */
	boolean put (int e, float x, float y, float r);

	/** Inserts the entity with the specified data into this {@link GridMap2DSimple}.
	 * @param e The id of the entity.
	 * @param x The x-coordinate of the entity.
	 * @param y The y-coordinate of the entity.
	 * @param r The collision-radius of the entity.
	 * @param gf The flags assigned to the entity.
	 * @return True if it was inserted. False if it couldn't be inserted, was already inserted or is out of bounds. */
	boolean put (int e, float x, float y, float r, int gf);

	/**
	 * Checks if entity e is contained inside this {@link GridMap2DSimple}.
	 * @param e The id of the entity that should be checked.
	 * @return True if e is contained in this {@link GridMap2DSimple} otherwise false.
	 */
	boolean contains (int e);

	/** Tries to remove the entity with matching id.
	 * @param e The id of the entity that should be removed.
	 * @return True if successful or false if the entity could not be found. */
	boolean remove (int e);

	/** Updates the position of the specified entity using the new supplied coordinates.
	 * @param e The id of the entity to be updated.
	 * @param x The new x-position of the entity.
	 * @param y The new y-position of the entity.
	 * @return True if successful or false if the entity does not belong to this map anymore. */
	boolean update (int e, float x, float y);

}
