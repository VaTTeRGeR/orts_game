package de.vatterger.entitysystem.networkmessages;

import com.badlogic.gdx.math.Vector3;

public class D_Camera {
	/**The center-coordinates of the players camera and the zoom-value stored in the z-component*/
	public Vector3 camProperties;
	
	public D_Camera() {
	}
	
	public D_Camera(Vector3 camProperties) {
		this.camProperties = camProperties;
	}
}