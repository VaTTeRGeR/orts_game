package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Quaternion;

public class Rotation extends Component {
	public Quaternion v[];

	public Rotation(Quaternion rotation) {
		v = new Quaternion[1];
		this.v[0] = rotation;
	}
	
	public Rotation(Quaternion rotation[]) {
		this.v = rotation;
	}
}
