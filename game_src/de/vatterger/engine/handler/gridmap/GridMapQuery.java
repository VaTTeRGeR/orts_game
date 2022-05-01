
package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

public class GridMapQuery {

	protected int size;

	protected int[] idArray = null;
	protected float[] colArray = null;

	public GridMapQuery () {
		this(64, true, true);
	}

	public GridMapQuery (int initialCapacity, boolean collectIds, boolean collectCollisionData) {

		if (collectIds) {
			idArray = new int[initialCapacity];
		}

		if (collectCollisionData) {
			colArray = new float[initialCapacity * 3];
		}
	}

	protected void put (int e) {

		if (idArray.length == size) {
			idArray = Arrays.copyOf(idArray, idArray.length * 2);
		}

		idArray[size++] = e;
	}

	protected void put (float x, float y, float r) {

		if (colArray.length == size * 3) {
			colArray = Arrays.copyOf(colArray, colArray.length * 2);
		}

		int sizeOffset = size * 3;

		colArray[sizeOffset + 0] = x;
		colArray[sizeOffset + 1] = y;
		colArray[sizeOffset + 2] = r;

		size++;
	}

	protected void put (int e, float x, float y, float r) {

		if (idArray.length == size) {
			idArray = Arrays.copyOf(idArray, idArray.length * 2);
			colArray = Arrays.copyOf(colArray, colArray.length * 2);
		}

		idArray[size] = e;

		final int sizeOffset = size * 3;

		colArray[sizeOffset + 0] = x;
		colArray[sizeOffset + 1] = y;
		colArray[sizeOffset + 2] = r;

		size++;
	}

	/**
	 * @return The number of data points stored in this {@link GridMapQuery}.
	 */
	public int getSize () {
		return size;
	}

	/** @return An array containing {@link #getSize()} number of valid ids. This means the arrays content is
	 *         [id1,id2,id3...idN...invalid data]. */
	public int[] getIdData () {
		return idArray;
	}

	/** Get the collected collision data in form of an float array.
	 * @return An array containing {@link #getSize()} number of valid triplets of this form: [x,y,radius]. This means the arrays
	 *         content is [x1,y1,r1,x2,y2,r2...xN,yN,rN...invalid data] */
	public float[] getCollisionData () {
		return colArray;
	}

	/** Clears the {@link GridMapQuery} so that it can be filled with data again. Size will be zero after this operation. */
	public void clear () {
		size = 0;
	}
}
