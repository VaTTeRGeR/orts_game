package de.vatterger.engine.util;

import com.badlogic.gdx.Gdx;

public class Metrics {
	private Metrics(){}
	
	public static final float ymod = (float)Math.sqrt(0.5d);
	public static final float sssp = 1000f;
	public static final float sssm = 100f;
	public static final float ppm = sssp/sssm;
	public static final float mpp = 1f/ppm;

	public static int wv = Gdx.graphics.getWidth();
	public static int hv = Gdx.graphics.getHeight();

	public static float ww = wv * mpp;
	public static float hw = hv * mpp;

}
