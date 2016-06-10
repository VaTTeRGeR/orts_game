package de.vatterger.techdemo.network.packets.client;

import com.badlogic.gdx.math.Rectangle;

public class ViewportUpdate {
	public Rectangle view;
	public ViewportUpdate() {
	}
	
	public ViewportUpdate(Rectangle view) {
		this.view = view;
	}
}