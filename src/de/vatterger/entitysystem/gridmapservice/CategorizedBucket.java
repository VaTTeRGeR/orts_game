package de.vatterger.entitysystem.gridmapservice;

import com.artemis.Entity;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.Main;

public class CategorizedBucket {
	private Bag<Entity> objBag = new Bag<Entity>(16);
	private Bag<GridFlag> flagBag = new Bag<GridFlag>(16);

	public void add(Entity e, GridFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<Entity> getAllWithSimilarFlag(GridFlag gf, Bag<Entity> fillBag) {
		for (int i = 0; i < objBag.size(); i++) {
			if(flagBag.get(i).hasAllFlagsOf(gf.flag())) {
				fillBag.add(objBag.get(i));
			}
		}
		return fillBag;
	}
	
	public void clear() {
		objBag.clear();
		flagBag.clear();
	}
}
