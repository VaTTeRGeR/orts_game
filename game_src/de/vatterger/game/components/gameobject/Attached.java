package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Attached extends Component {
	public int parentId;
	public Vector3 offset;
	
	public Attached(int parentId, Vector3 offset) {
		this.parentId = parentId;
		this.offset = offset;
	}
}
