package de.vatterger.engine.handler.terrain;

public class TerrainTileMeshData {

	public final int tileIndex, tileLayer;
	
	public final float[] vertices;
	public final short[] indices;

	public TerrainTileMeshData (int tileIndex, int tileLayer, float[] vertices, short[] indices) {
		this.tileIndex = tileIndex;
		this.tileLayer = tileLayer;
		this.vertices = vertices;
		this.indices = indices;
	}
}
