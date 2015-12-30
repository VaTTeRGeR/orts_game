package de.vatterger.entitysystem.components.client;


import com.artemis.Component;

public class LocalRotation extends Component {
	public float rot;

	public LocalRotation() {
	}
	
	public LocalRotation(float rot) {
		this.rot = rot;
	}
}
