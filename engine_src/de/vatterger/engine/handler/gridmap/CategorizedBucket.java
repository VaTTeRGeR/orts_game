package de.vatterger.engine.handler.gridmap;

import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;

final public class CategorizedBucket {
	private IntBag objBag = new IntBag(0);
	private Bag<GridMapBitFlag> flagBag = new Bag<GridMapBitFlag>(0);

	public void add(int e, GridMapBitFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public IntBag getAllWithSimilarFlag(GridMapBitFlag gf, IntBag fillBag) {
		for (int i = objBag.size()-1; i >= 0 ; i--) {
			int objBagInt = objBag.get(i);
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
