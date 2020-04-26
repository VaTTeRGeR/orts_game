package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class StaticObject extends Component {
	public static final StaticObject SHARED_INSTANCE = new StaticObject();
}
