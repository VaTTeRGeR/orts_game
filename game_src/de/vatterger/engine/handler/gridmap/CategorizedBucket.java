package de.vatterger.engine.handler.gridmap;

import com.artemis.utils.IntBag;

final public class CategorizedBucket {
	
	private IntBag objBag = new IntBag(1);
	private IntBag flagBag = new IntBag(1);

	public void add(int e, int gf) {
		objBag.add(e);
		flagBag.add(gf);
	}
	
	public IntBag getAllWithSimilarFlag(int gf, IntBag fillBag) {
		
		for (int i = 0; i < objBag.size() ; i++) {
			
			int objBagItem = objBag.get(i);
			
			if(GridMapUtil.isContaining(flagBag.get(i), gf)/* &! fillBag.contains(objBagItem)*/) {
				fillBag.add(objBagItem);
			}
		}
		
		return fillBag;
	}
	
	public void clear() {
		objBag.setSize(0);
		flagBag.setSize(0);
	}
}
