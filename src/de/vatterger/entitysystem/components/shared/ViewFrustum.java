package de.vatterger.entitysystem.components.shared;


import com.artemis.Component;
import com.badlogic.gdx.math.Rectangle;

public class ViewFrustum extends Component {

	public Rectangle rect;
	
	public ViewFrustum() {
		rect = new Rectangle();
	}

	public ViewFrustum(Rectangle rect) {
		this();
		this.rect.set(rect);
	}
}
