package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteLayer extends Component {
	public static final int CELLAR = -1;
	public static final int GROUND0 = 0;
	public static final int GROUND1 = 1;
	public static final int OBJECTS0 = 2;
	public static final int OBJECTS1 = 3;
	public static final int OBJECTS2 = 4;
	public static final int OBJECTS3 = 5;
	public static final int OBJECTS4 = 6;
	public static final int OBJECTS5 = 7;
	public static final int PLANE0 = 8;
	public static final int PLANE1 = 9;
	public static final int PLANE2 = 10;
	
	public int v;

	public SpriteLayer() {}
	
	public SpriteLayer(int layer) {
		this.v = layer;
	}
}
