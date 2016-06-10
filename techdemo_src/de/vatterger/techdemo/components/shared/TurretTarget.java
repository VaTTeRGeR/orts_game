package de.vatterger.techdemo.components.shared;

import com.artemis.Component;

public class TurretTarget extends Component {
	public int target;

	public TurretTarget() {
		target = -1;
	}

	public TurretTarget(int target) {
		this.target = target;
	}

}
