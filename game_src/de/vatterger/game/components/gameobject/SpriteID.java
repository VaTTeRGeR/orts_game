package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class SpriteID extends Component {
	public int id[];
	public Vector3 offset[];

	public SpriteID(int id) {
		this(new int[]{id});
	}

	public SpriteID(int id[]) {
		this(id, null);
	}

	public SpriteID(int id[], Vector3[] offset) {
		this.id = id;
		this.offset = offset;
	}
}
