package de.vatterger.entitysystem.components.shared;

import com.artemis.Component;

public class ViewRange extends Component {
	public float range = 0f;

	public ViewRange(float range) {
		this.range = range;
	}
}
