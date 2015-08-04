package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.artemis.utils.Bag;

public final class RemoteMaster extends Component {
	
	public boolean changed;
	public boolean rebuildComponents;
	public Bag<Class<? extends Component>> classes;
	public Bag<Component> components;

	public RemoteMaster() {
		this(1);
	}
	
	public RemoteMaster(int size) {
		classes = new Bag<Class<? extends Component>>(size);
		components = new Bag<Component>(size);
		setIsChanged(true);
		setNeedsRebuild(true);
	}
	
	@SafeVarargs
	public RemoteMaster(Class<? extends Component>... c) {
		this(c.length);
		for (int i = 0; i < c.length; i++) {
			add(c[i]);
		}
	}
	
	public RemoteMaster add(Class<? extends Component> c) {
		setNeedsRebuild(true);
		classes.add(c);
		return this;
	}
	
	public boolean remove(Class<? extends Component> c){
		setNeedsRebuild(true);
		return classes.remove(c);
	}
	
	public boolean getIsChanged() {
		return changed;
	}
	
	public void setIsChanged(boolean isChanged) {
		changed = isChanged;
	}

	public boolean getNeedsRebuild() {
		return rebuildComponents;
	}
	
	public void setNeedsRebuild(boolean needsRebuild) {
		rebuildComponents = needsRebuild;
	}
}
