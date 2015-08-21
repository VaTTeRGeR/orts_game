package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Modifiable;

public final class RemoteMaster extends Component {
	
	public boolean changed;
	public boolean rebuildComponents;
	public Bag<Class<? extends Component>> classes;
	public Bag<Modifiable> components;

	public RemoteMaster() {
		this(1);
	}
	
	public RemoteMaster(int size) {
		classes = new Bag<Class<? extends Component>>(size);
		components = new Bag<Modifiable>(size);
		setIsChanged(true);
		setNeedsRebuild(true);
	}
	
	@SafeVarargs
	public RemoteMaster(Class<? extends Component>... c) {
		this(c.length);
		for (int i = 0; i < c.length; i++) {
			add(c[i], false);
		}
	}
	
	public RemoteMaster add(Class<? extends Component> c, boolean optional) {
		setNeedsRebuild(true);
		classes.add(c);
		return this;
	}
	
	public void remove(Class<? extends Component> c){
		setNeedsRebuild(true);
		classes.remove(c);
	}
	
	public boolean getIsChanged() {
		return changed;
	}
	
	public void setIsChanged(boolean isChanged) {
		changed = isChanged;
	}
	
	public void detectChanges() {
		changed = false;
		for (int i = 0; i < components.size(); i++) {
			if(components.get(i).getIsModified())
				changed = true;
		}
	}

	public boolean getNeedsRebuild() {
		return rebuildComponents;
	}
	
	public void setNeedsRebuild(boolean needsRebuild) {
		rebuildComponents = needsRebuild;
	}
}
