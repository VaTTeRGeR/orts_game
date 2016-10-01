package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class TracerTarget extends Component {
	public Vector3 targetPos;
	public float dist = Float.MAX_VALUE;

	public TracerTarget(float x, float y, float z) {
		targetPos = new Vector3(x, y, z);
	}
}
