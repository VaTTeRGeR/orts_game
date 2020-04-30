package de.vatterger.engine.handler.gridmap;

public class GridMapEntry {
	public int e,gf;
	public float x,y,r;
	
	public GridMapEntry () {
		this(-1, 0, 0f, 0f, 0f);
	}
	
	public GridMapEntry (int e, int gf, float x, float y, float r) {
		this.e = e;
		this.gf = gf;
		this.x = x;
		this.y = y;
		this.r = r;
	}
}
