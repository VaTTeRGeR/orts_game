package de.vatterger.entitysystem.components.server;

import com.artemis.Component;
import com.artemis.utils.Bag;

public class EntityAckBucket extends Component {
	public Bag<Integer> ids = new Bag<Integer>(256);
}
