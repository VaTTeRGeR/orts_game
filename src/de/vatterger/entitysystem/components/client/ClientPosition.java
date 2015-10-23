package de.vatterger.entitysystem.components.client;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.interfaces.Interpolatable;

public class ClientPosition extends Component implements Interpolatable<Vector3> {
	private Vector3 posOld = null, posLerp = null, posTarget = null;
	private float deltaAccumulated = 0f, interpolationTime = 0f;

	public ClientPosition(Vector3 pos) {
		this.posLerp = new Vector3(pos);
		this.posOld = new Vector3(pos);
		this.posTarget = new Vector3(pos);
		interpolationTime = GameConstants.INTERPOLATION_PERIOD;
	}

	@Override
	public void updateInterpolation(float delta, Vector3 target) {
		if(!posTarget.equals(target)) { //Target changed or EXTRAPOLATION GRACE PERIOD EXCEEDED!
			posOld.set(posLerp);
			posTarget.set(target);
			deltaAccumulated = 0;
			interpolationTime = GameConstants.INTERPOLATION_PERIOD_MEASURED;
		} else if(deltaAccumulated/interpolationTime > GameConstants.EXTRAPOLATION_FACTOR) {
			posLerp.set(target);
			posOld.set(target);
			posTarget.set(target);
		}
		deltaAccumulated += delta;
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
}
