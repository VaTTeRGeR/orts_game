package de.vatterger.entitysystem.gridmapservice;

import com.artemis.utils.Bag;

public class CategorizedBucket<T>{
	private Bag<T> objBag = new Bag<T>(16);
	private Bag<GridFlag> flagBag = new Bag<GridFlag>(16);

	public void add(T e, GridFlag gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public Bag<T> getAllWithSimilarFlag(GridFlag gf, Bag<T> fillBag) {
		for (int i = objBag.size()-1; i >= 0 ; i--) {
			if(flagBag.get(i).hasFlagsOf(gf.flag()))
				fillBag.add(objBag.get(i));
		}
		return fillBag;
	}
	
	public void clear() {
		objBag.clear();
		flagBag.clear();
	}
}
