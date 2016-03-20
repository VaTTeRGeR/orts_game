package de.vatterger.entitysystem.components.shared;

import com.artemis.Component;

public final class VehicleProperties extends Component {
	public float speed_max,speed_turn;

	public VehicleProperties(float speed_max, float speed_turn) {
		this.speed_max = speed_max;
		this.speed_turn = speed_turn;
	}
}
