package de.vatterger.game.components.unit;

import com.artemis.Component;

public class Target extends Component {
	public int[] v;

	public Target(int[] target) {
		this.v = target;
	}
}
