package de.vatterger.entitysystem.components.server;

import java.util.HashMap;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class ComponentVersioningRegister extends Component {
	Bag<HashMap<Modifiable, Integer>> mapRegister = new Bag<HashMap<Modifiable,Integer>>(512);
	
	public boolean getHasChanged(Entity e, Modifiable c) {
		HashMap<Modifiable, Integer> map = null;
		if((map = mapRegister.safeGet(e.id)) == null) {
			mapRegister.set(e.id, new HashMap<Modifiable,Integer>(8));
			map = mapRegister.get(e.id);
		}
		Integer oldVersion = null;
		if((oldVersion = map.get(c)) == null) {
			map.put(c, c.getModifiedVersion());
			return true;
		} else if(c.getIsModified(oldVersion)){
			map.put(c, c.getModifiedVersion());
			return true;
		} else {
			return false;
		}
	}
}
