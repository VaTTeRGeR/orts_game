package de.vatterger.techdemo.components.client;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.interfaces.Interpolatable;
import de.vatterger.techdemo.util.GameUtil;

public class InterpolatedPosition extends Component implements Interpolatable<Vector3> {
	private Vector3 posOld = null, posLerp = null, posTarget = null;
	private float deltaAccumulated = 0f, interpolationTime = 0f;

	public InterpolatedPosition(Vector3 pos) {
		this.posLerp = new Vector3(pos);
		this.posOld = new Vector3(pos);
		this.posTarget = new Vector3(pos);
		interpolationTime = GameConstants.INTERPOLATION_PERIOD_MIN;
	}

	@Override
	public void updateInterpolation(float delta, Vector3 target, boolean newUpdate) {
		if(newUpdate) { //Target has changed => new update came in => reset counters
			//start from where we are currently
			posOld.set(posLerp);
			//set new target
			posTarget.set(target);
			//reset the accumulated time since the last position update
			deltaAccumulated = 0;
			//set the time that will probably pass till we receive another update-packet
			//GameConstants.INTERPOLATION_PERIOD_MEASURED is an average value, but you can also just use deltaAccumulated before you set it to zero
			interpolationTime = GameConstants.INTERPOLATION_PERIOD_MEASURED;
		} else if(deltaAccumulated/interpolationTime > GameConstants.EXTRAPOLATION_FACTOR) {//Extrapolation-period expired, just jump to the target position
			posLerp.set(target);
			posOld.set(target);
			posTarget.set(target);
		}
		//advance the interpolation time
		deltaAccumulated += delta;
		//interpolate
		posLerp.set(posOld);
		posLerp.lerp(target, deltaAccumulated/interpolationTime);
		
		/*System.out.println("lerp: "+deltaAccumulated/GameConstants.INTERPOLATION_PERIOD);
		System.out.println("deltaAccumulated: "+deltaAccumulated);
		System.out.println("old: "+posOld);
		System.out.println("lerp: "+posLerp);
		System.out.println("target: "+target);
		System.out.println("---");*/
	}
	
	@Override
	public Vector3 getInterpolatedValue() {
		return posLerp;
	}
	
	public void draw(Camera camera, ImmediateModeRenderer20 lineRenderer){
		lineRenderer.begin(camera.combined, GL20.GL_LINES);
		
		Color c0 = Color.RED;
		GameUtil.line(posTarget,posTarget.cpy().add(0, 0, 5),c0, lineRenderer);

		Color c1 = Color.BLUE;
		GameUtil.line(posOld,posOld.cpy().add(0, 0, 5),c1, lineRenderer);
		
		Color c2 = Color.GREEN;
		GameUtil.line(posLerp,posLerp.cpy().add(0, 0, 5),c2, lineRenderer);

		lineRenderer.end();
	}
}
