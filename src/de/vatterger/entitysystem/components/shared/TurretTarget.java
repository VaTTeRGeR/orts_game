package de.vatterger.entitysystem.components.shared;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class TurretTarget extends Component {
	public int target;

	public TurretTarget() {
		target = -1;
	}

	public TurretTarget(int target) {
		this.target = target;
	}

}
