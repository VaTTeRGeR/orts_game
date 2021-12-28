package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class CullMetersPerPixel extends Component {

	public float mpp;
	
	public CullMetersPerPixel() {}
	
	public CullMetersPerPixel(float v) {
		this.mpp = v;
	}
}
