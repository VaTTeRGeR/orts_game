package de.vatterger.entitysystem.gridmapservice;

import com.artemis.utils.Bag;

public class CategorizedBucket<T>{
	Bag<Bag<T>> registers = new Bag<Bag<T>>(GridFlag.NUMFLAGS);
	public CategorizedBucket() {
		for (int i = 0; i < GridFlag.NUMFLAGS; i++) {
			registers.add(new Bag<T>());
		}
	}
	public void add(T obj, GridFlag gf) {
		for (int i = 0; i < GridFlag.NUMFLAGS; i++) {
			if(gf.isFlagSet(i+1)) {
				registers.get(i).add(obj);
			}
		}
	}
	public void clear() {
		for (int i = 0; i < GridFlag.NUMFLAGS; i++) {
			registers.get(i).clear();
		}
	}
}
