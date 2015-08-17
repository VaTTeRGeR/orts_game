package de.vatterger.entitysystem.networkmessages;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class ClientViewportUpdate {
	public Rectangle view;
	public ClientViewportUpdate() {
		view = new Rectangle();
	}
	
	public ClientViewportUpdate(Rectangle view) {
		this.view.set(view);
	}
}