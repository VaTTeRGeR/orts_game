package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class RemoveTimed extends Component {
	public float timeLeft;

	public RemoveTimed() {}

	public RemoveTimed(float time) {
		this.timeLeft = time;
	}
}
