package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Target extends Component {
	public int[] v;

	public Target(int[] target) {
		this.v = target;
	}
}
