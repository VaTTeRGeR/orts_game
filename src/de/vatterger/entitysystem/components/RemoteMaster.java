package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Modifiable;

public final class RemoteMaster extends Component {
	
	public boolean rebuildComponents;
	public Bag<Class<? extends Component>> classes;
	public Bag<Modifiable> components;

	public RemoteMaster() {
		this(1);
	}
	
	public RemoteMaster(int size) {
		classes = new Bag<Class<? extends Component>>(size);
		components = new Bag<Modifiable>(size);
	}
	
	@SafeVarargs
	public RemoteMaster(Class<? extends Component>... c) {
		this(c.length);
		for (int i = 0; i < c.length; i++) {
			classes.add(c[i]);
		}
	}
}
