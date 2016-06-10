package de.vatterger.techdemo.components.shared;


import com.artemis.Component;
import com.badlogic.gdx.math.Rectangle;

public class NetSynchedArea extends Component {

	public Rectangle rect;
	
	public NetSynchedArea() {
		rect = new Rectangle();
	}

	public NetSynchedArea(Rectangle rect) {
		this();
		this.rect.set(rect);
	}
}
