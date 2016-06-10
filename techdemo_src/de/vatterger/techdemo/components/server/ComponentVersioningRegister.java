package de.vatterger.techdemo.components.server;

import java.util.HashMap;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.utils.Bag;

import de.vatterger.techdemo.interfaces.Versionable;

public class ComponentVersioningRegister extends Component {
	Bag<HashMap<Versionable, Integer>> mapRegister = new Bag<HashMap<Versionable,Integer>>(512);

	public static Bag<ComponentVersioningRegister> cvrs = new Bag<ComponentVersioningRegister>(16);
	
	public boolean getHasChanged(Entity e, Versionable c) {
		HashMap<Versionable, Integer> map = null;
		//Get or create a Component to Version map for the "Entity e"
		if((map = mapRegister.safeGet(e.getId())) == null) {
			mapRegister.set(e.getId(), new HashMap<Versionable,Integer>(8));
			map = mapRegister.get(e.getId());
		}
		Integer oldVersion = null;
		//Compare stored version with version of "Versionable c"
		if((oldVersion = map.get(c)) == null) {
			map.put(c, c.getVersion());
			return true;
		} else if(c.compareVersion(oldVersion)){
			map.put(c, c.getVersion());
			return true;
		} else {
			return false;
		}
	}
	
	public static void clearFor(Entity e){
		for (int i = 0; i < cvrs.size(); i++) {
			if(cvrs.get(i) != null) {
				cvrs.get(i).clear(e);
			}
		}
	}
	
	private void clear(Entity e) {
		HashMap<Versionable, Integer> map = null;
		if((map = mapRegister.safeGet(e.getId())) != null) {
			map.clear();
		}
	}
}
