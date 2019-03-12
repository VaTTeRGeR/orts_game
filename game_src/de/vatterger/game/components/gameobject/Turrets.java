package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Turrets extends Component {
	
	public int[] turretIds;

	public Turrets() {}
	
	public Turrets(int turretsSize) {
		this.turretIds = new int[turretsSize];
	}
}
