package de.vatterger.entitysystem.util.map;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.util.GameUtil;

public class GridPartitionMap<T> {

	private Bag<Bag<Bucket<T>>> buckets = new Bag<Bag<Bucket<T>>>();
	private int cellSize;
	
	public GridPartitionMap(int worldSize, int expectedUnitCount) {
		cellSize = GameUtil.optimalCellSize(worldSize, expectedUnitCount);
	}
	
	public void insert(Vector2 v, T e) {
		getBucket(cell(v.x), cell(v.y)).add(e);
	}

	public void insert(Circle c, T e) {
		insert(GameUtil.circleToRectangle(c), e);
	}

	public void insert(Rectangle r, T e) {
		final int startX = cell(r.x), endX = cell(r.x+r.width);
		final int startY = cell(r.y), endY = cell(r.y+r.height);
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
		Bag<Bucket<T>> bbx = buckets.safeGet(cx);
		if(bbx == null) {
			buckets.set(cx, bbx = new Bag<Bucket<T>>(1));
		}
		Bucket<T> by = bbx.safeGet(cy);
		if(by == null) {
			bbx.set(cy, by = new Bucket<T>());
		}
		return by;
	}

	public Bucket<T> getBucketsMerged(Rectangle r) {
		Bucket<T> all = new Bucket<T>(4);
		Bag<Bucket<T>> buckets = getBuckets(r);
		for (int i = 0; i < buckets.size(); i++) {
			all.addAll(buckets.get(i));
		}
		return all;
	}
	
	public Bag<Bucket<T>> getBuckets(Rectangle r) {
		Bag<Bucket<T>> bag = new Bag<Bucket<T>>(8);
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				bag.add(getBucket(x, y));
			}
		}
		return bag;
	}
	
	public void clear() {
		int sx = buckets.size(), sy;
		Bucket<T> bucket = null;
		for (int x = 0; x < sx; x++) {
			if (buckets.get(x) == null) {
				continue;
			} else {
				sy = buckets.get(x).size();
				for (int y = 0; y < sy; y++) {
					bucket = buckets.get(x).get(y);
					if (bucket != null) {
						bucket.clear();
					}
				}
			}
		}
	}
		
	private int cell(float p) {
		return (int)(p >= 0 ? p/cellSize : 0);
	}
}
