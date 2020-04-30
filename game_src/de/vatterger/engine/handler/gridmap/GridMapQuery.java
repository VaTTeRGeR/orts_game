package de.vatterger.engine.handler.gridmap;

import java.util.Arrays;

public class GridMapQuery {
	
	protected int size;

	protected int[] idArray = null;
	protected float[] colArray = null;
	
	public GridMapQuery () {
		this(16, true, true);
	}

	public GridMapQuery (int capacity, boolean collectIds, boolean collectCollisionData) {
		
		if(collectIds) {
			idArray = new int[capacity];
		}
		
		if(collectCollisionData) {
			colArray = new float[capacity * 3];
		}
	}
	
	protected void put(int e) {
		
		if(idArray.length == size) {
			idArray = Arrays.copyOf(idArray, idArray.length*2);
		}
		
		idArray[size++] = e;
	}
	
	protected void put(float x, float y, float r) {
		
		if(colArray.length == size * 3) {
			colArray = Arrays.copyOf(colArray, colArray.length*2);
		}
		
		final int sizeOffset = size * 3;
		
		colArray[sizeOffset + 0] = x;
		colArray[sizeOffset + 1] = y;
		colArray[sizeOffset + 2] = r;
		
		size++;
	}
	
	protected void put(int e, float x, float y, float r) {
		
		if(idArray.length == size) {
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
	
	public int size() {
		return size;
	}
	
	public int[] idData() {
		return idArray;
	}
	
	public float[] colData() {
		return colArray;
	}
	
	public void clear() {
		size = 0;
	}
}
