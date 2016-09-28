package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class BlendMode extends Component {
	public int src;
	public int dst;

	public BlendMode(int src, int dst) {
		this.src = src;
		this.dst = dst;
	}
}
