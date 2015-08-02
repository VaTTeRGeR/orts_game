package de.vatterger.entitysystem.tools;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class SpatialVector3Map<T> {

	Bag<Bag<Bucket<T>>> buckets = new Bag<Bag<Bucket<T>>>(1);
	int cellsize;
	
	public SpatialVector3Map(int cellsize) {
		if(cellsize > 0) {
			this.cellsize = cellsize;
		} else {
			this.cellsize = 64;
		}
	}
	
	public void insert(Vector3 v, T e) {
		Bucket<T> b = getBucket(cell(v.x), cell(v.y));
		b.add(e);
	}

	public void insert(Circle c, T e) {
		insert(GameUtil.circleToRectangle(c), e);
	}

	public void insert(Rectangle r, T e) {
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucket(x, y).add(e);
			}
		}
	}
	
	public Bucket<T> getBucket(float wx, float wy){
		return getBucket(cell(wx),cell(wy));
	}
	
	public Bucket<T> getBucket(int cx, int cy){
		if(buckets.safeGet(cx) == null) {
			buckets.set(cx, new Bag<Bucket<T>>(1));
		}
		if(buckets.get(cx).safeGet(cy) == null) {
			buckets.get(cx).set(cy, new Bucket<T>(16));
		}
		return buckets.get(cx).get(cy);
	}
	
	public Bucket<T> getBucket(Rectangle r) {
		Bucket<T> b = new Bucket<T>();
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				b.addAll(getBucket(x, y));
			}
		}
		return b;
	}
	
	private boolean hasBucket(int x, int y) {
		return (buckets.get(x) != null && buckets.get(x).get(y) != null);
	}
	
	public void clear() {
		for (int x = 0; x < buckets.size(); x++) {
			if(buckets.get(x) == null)
				continue;
			for (int y = 0; y < buckets.get(x).size(); y++) {
				if(hasBucket(x, y)) {
					buckets.get(x).get(y).clear();
				}
			}
		}
	}
	
	private int cell(float p) {
		if(p >= 0)
			return (int)(p/cellsize);
		else
			return (int)(-p/cellsize);
	}
}
