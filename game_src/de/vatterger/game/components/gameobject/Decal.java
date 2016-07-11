package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Decal extends Component {
	public int v[] = new int[3];

	public Decal(int id, int srcFunc, int destFunc) {
		this.v[0] = id;
		this.v[1] = srcFunc;
		this.v[2] = destFunc;
	}
	public Decal(int id) {
		this.v[0] = id;
		this.v[1] = -1;
		this.v[2] = -1;
	}
}
