package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class TracerTarget extends Component {
	
	public Vector3 targetPos = new Vector3();
	
	public float spreadX = 0;
	public float spreadY = 0;
	
	public float lastDist = Float.MAX_VALUE;

	public TracerTarget() {}
	
	public TracerTarget(float x, float y, float z) {
		targetPos.set(x, y, z);
	}
	
	public TracerTarget setSpread(float spreadXY) {

		spreadX = spreadY = spreadXY;
		
		return this;
	}
	
	public TracerTarget setSpread(float spreadX, float spreadY) {

		this.spreadX = spreadX;
		this.spreadY = spreadY;
		
		return this;
	}
}
