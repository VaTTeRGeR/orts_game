package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Firing extends Component {
	public boolean[] v;

	public Firing() {}
	
	public Firing(boolean[] firing) {
		this.v = firing;
	}
}
