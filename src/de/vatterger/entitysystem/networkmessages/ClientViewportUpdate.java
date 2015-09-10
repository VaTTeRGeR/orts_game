package de.vatterger.entitysystem.networkmessages;

import com.badlogic.gdx.math.Rectangle;

public class ClientViewportUpdate {
	public Rectangle view;
	public ClientViewportUpdate() {
	}
	
	public ClientViewportUpdate(Rectangle view) {
		this.view = view;
	}
}