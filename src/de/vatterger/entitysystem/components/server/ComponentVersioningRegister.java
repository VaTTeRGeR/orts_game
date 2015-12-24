package de.vatterger.entitysystem.components.server;

import java.util.HashMap;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class ComponentVersioningRegister extends Component {
	Bag<HashMap<Modifiable, Integer>> cvmap = new Bag<HashMap<Modifiable,Integer>>(512);
	public boolean getHasChanged(Entity e, Modifiable c) {
		HashMap<Modifiable, Integer> map = null;
		if((map = cvmap.safeGet(e.id)) == null) {
			cvmap.set(e.id, new HashMap<Modifiable,Integer>(8));
			map = cvmap.get(e.id);
		}
		Integer version = null;
		if((version = map.get(c)) == null) {
			map.put(c, c.getModifiedVersion());
			return true;
		} else {
			boolean mod = c.getIsModified(version);
			
			if(mod)
				map.put(c, c.getModifiedVersion());

			return mod;
		}
	}
}
