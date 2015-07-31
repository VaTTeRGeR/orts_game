package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Target extends Component {
	/**The Target of the owning Entity*/
	public Vector3 target = new Vector3(Vector3.Zero);
	
	public Target() {
	}
	
	public Target(Vector3 target) {
		this.target = target;
	}
}
