package de.vatterger.entitysystem.components.server;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Versionable;

public final class RemoteMaster extends Component {
	
	public boolean rebuildComponents;
	public Bag<Class<? extends Component>> classes;
	public Bag<Versionable> components;

	public RemoteMaster() {
		this(1);
	}
	
	public RemoteMaster(int size) {
		classes = new Bag<Class<? extends Component>>(size);
		components = new Bag<Versionable>(size);
	}
	
	@SafeVarargs
	public RemoteMaster(Class<? extends Component>... c) {
		this(c.length);
		for (int i = 0; i < c.length; i++) {
			classes.add(c[i]);
		}
	}
}
