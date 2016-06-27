package de.vatterger.game.components.unit;

import com.artemis.Component;
import com.badlogic.gdx.math.Quaternion;

public class Rotation extends Component {
	public Quaternion v;

	public Rotation(Quaternion rotation) {
		this.v = rotation;
	}
}
