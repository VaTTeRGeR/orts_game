package de.vatterger.entitysystem.gridmapservice;

import com.artemis.Entity;
import com.artemis.utils.Bag;

public class CategorizedBucket {
	private Bag<Entity> objBag = new Bag<Entity>(16);
	private Bag<GridFlag> flagBag = new Bag<GridFlag>(16);

	public void add(Entity e, GridFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<Entity> getAllWithSimilarFlag(GridFlag gf, Bag<Entity> fillBag) {
		for (int i = objBag.size()-1; i >= 0 ; i--) {
			if(flagBag.get(i).hasAllFlagsOf(gf.flag()) &! fillBag.contains(objBag.get(i))) {
				fillBag.add(objBag.get(i));
			}
		}
		return fillBag;
	}
	
	public void clear() {
		objBag.clear();
		flagBag.clear();
	}

	public void remove(Entity e) {
		Entity[] ents = (Entity[])objBag.getData();
		for (int i = 0; i < ents.length; i++) {
			if(ents[i].equals(e)) {
				objBag.remove(i);
				flagBag.remove(i);
				return;
			}
		}
	}
}
