package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Quaternion;

public class Rotation extends Component {
	public Quaternion v1[] = null;
	public String v2[] = null;

	public Rotation() {}
	
	public Rotation(Quaternion rot){
		set(rot);
	}
	
	public Rotation set(Quaternion ...rot) {
		this.v1 = rot;
		return this;
	}

	public Rotation set(String ...name) {
		this.v2 = name;
		return this;
	}
}
