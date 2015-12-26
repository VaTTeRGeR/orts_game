package de.vatterger.entitysystem.components.client;


import com.artemis.Component;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.interfaces.Interpolatable;

public class ClientRotation extends Component implements Interpolatable<Float>{
	private float rotOld = 0f, rotLerp = 0f, rotTarget = 0f;
	private float deltaAccumulated = 0f, interpolationTime = 0f;

	public ClientRotation(Float rot) {
		this.rotLerp = rot;
		this.rotOld = rot;
		this.rotTarget = rot;
		interpolationTime = GameConstants.INTERPOLATION_PERIOD_TARGET;
	}

	@Override
	public void updateInterpolation(float delta, Float target, boolean newUpdate) {
		if(newUpdate) { //Target changed or EXTRAPOLATION GRACE PERIOD EXCEEDED!
			rotOld = rotLerp;
			rotTarget = target;
			deltaAccumulated = 0;
			interpolationTime = GameConstants.INTERPOLATION_PERIOD_MEASURED;
		} else if(deltaAccumulated/interpolationTime > 1) {
			rotLerp = target;
			rotOld = target;
			rotTarget = target;
		}
		deltaAccumulated += delta;
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

	@Override
	public Float getInterpolatedValue() {
		return rotLerp;
	}
}
