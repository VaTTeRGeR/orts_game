package de.vatterger.entitysystem.gridmapservice;

import com.artemis.utils.Bag;

public class CategorizedBucket {
	private Bag<Integer> objBag = new Bag<Integer>(16);
	private Bag<GridFlag> flagBag = new Bag<GridFlag>(16);

	public void add(Integer e, GridFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<Integer> getAllWithSimilarFlag(GridFlag gf, Bag<Integer> fillBag) {
		for (int i = objBag.size()-1; i >= 0 ; i--) {
			if(flagBag.get(i).isSuperSetOf(gf.flagValue()) &! fillBag.contains(objBag.get(i))) {
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
