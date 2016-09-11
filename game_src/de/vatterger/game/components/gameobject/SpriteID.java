package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteID extends Component {
	public int id[];
	public float offset[];

	public SpriteID(int id) {
		this(new int[]{id});
	}

	public SpriteID(int id[]) {
		this(id, null);
	}

	public SpriteID(int id[], float[] offset) {
		this.id = id;
		this.offset = offset;
	}
}
