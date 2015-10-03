package de.vatterger.entitysystem.gridmapservice;

import com.artemis.utils.Bag;

public class CategorizedBucket {
	private Bag<Integer> objBag = new Bag<Integer>(16);
	private Bag<BitFlag> flagBag = new Bag<BitFlag>(16);

	public void add(Integer e, BitFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<Integer> getAllWithSimilarFlag(BitFlag gf, Bag<Integer> fillBag) {
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
