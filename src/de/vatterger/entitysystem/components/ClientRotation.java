package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.interfaces.Interpolatable;

public class ClientRotation extends Component implements Interpolatable<Float>{
	private float rotOld = 0f, rotLerp = 0f;
	private float deltaAccumulated = 0f, interpolationTime = 0f;

	public ClientRotation(Float rot) {
		this.rotLerp = rot;
		this.rotOld = rot;
		interpolationTime = GameConstants.INTERPOLATION_PERIOD;
	}

	@Override
	public void updateInterpolation(float delta, Float target) {
		deltaAccumulated += delta;
		if(deltaAccumulated > interpolationTime || Math.abs(rotLerp - target) < 1) {
			rotOld = rotLerp;
			deltaAccumulated = 0;
			interpolationTime = GameConstants.INTERPOLATION_PERIOD_MEASURED;
		} else {
			if(Math.abs(target-rotOld) > 180) {
				if(rotOld > target)
					target += 360;
				else
					rotOld += 360;
			}
			
			rotLerp = MathUtils.lerp(rotOld, target, deltaAccumulated/interpolationTime);

			rotLerp = rotLerp % 360f;
			rotOld = rotOld % 360f;
		}		
		
		/*System.out.println("lerp: "+deltaAccumulated/GameConstants.INTERPOLATION_PERIOD);
		System.out.println("deltaAccumulated: "+deltaAccumulated);
		System.out.println("old: "+posOld);
		System.out.println("lerp: "+posLerp);
		System.out.println("target: "+target);
		System.out.println("---");*/
	}

	@Override
	public Float getInterpolatedValue() {
		return rotLerp;
	}
}
