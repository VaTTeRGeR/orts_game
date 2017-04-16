package de.vatterger.engine.util;

import com.badlogic.gdx.Gdx;

public class Metrics {
	private Metrics(){}
	
	/**	Multiplying this projects the y coordinate from world to screen coordinates*/
	public static final float ymodp = (float)Math.sqrt(0.5d);
	/**	Multiplying this projects the y coordinate from screen to world coordinates*/
	public static final float ymodu = 1f/ymodp;
	/** virtual sprite size (in) pixels */
	public static final float sssp = 1000f;
	/** virtual sprite size (in) meters */
	public static final float sssm = 100f;
	/** pixels per meter */
	public static final float ppm = sssp/sssm;
	/** meters per pixels*/
	public static final float mpp = 1f/ppm;

	/** Display width in pixels */
	public static int wv = Gdx.graphics.getWidth();
	/** Display height in pixels */
	public static int hv = Gdx.graphics.getHeight();

	/** Display width in meters */
	public static float ww = wv * mpp;
	/** Display height in meters */
	public static float hw = hv * mpp;

}
