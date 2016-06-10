package de.vatterger.techdemo.handler.gridmap;

import com.artemis.utils.Bag;

final public class CategorizedBucket {
	private Bag<Integer> objBag = new Bag<Integer>(0);
	private Bag<GridMapBitFlag> flagBag = new Bag<GridMapBitFlag>(0);

	public void add(Integer e, GridMapBitFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<Integer> getAllWithSimilarFlag(GridMapBitFlag gf, Bag<Integer> fillBag) {
		for (int i = objBag.size()-1; i >= 0 ; i--) {
			Integer objBagInt = objBag.get(i);
			if(flagBag.get(i).isContaining(gf.flagValue()) &! fillBag.contains(objBagInt)) {
				fillBag.add(objBagInt);
			}
		}
		return fillBag;
	}
	
	public void clear() {
		objBag.clear();
		flagBag.clear();
	}
}
