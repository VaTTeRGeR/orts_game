package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.badlogic.gdx.math.Vector3;

public class Attached extends Component {
	
	@EntityId public int parentId = -1;

	public int level = 0;
	
	public float rotation;
	public Vector3 offset;
	
	public Attached() {}
	
	public Attached(int parentId, Vector3 offset) {
		this(parentId, offset, 0f);
	}
	public Attached(int parentId, Vector3 offset, float rotation) {
		this.parentId = parentId;
		this.offset = offset;
		this.rotation = rotation;
	}
}
