package de.vatterger.entitysystem.network.messages;

import com.badlogic.gdx.math.Rectangle;

public class ClientViewportUpdate {
	public Rectangle view;
	public ClientViewportUpdate() {
	}
	
	public ClientViewportUpdate(Rectangle view) {
		this.view = view;
	}
}