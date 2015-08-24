package de.vatterger.entitysystem.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SpatialHashingMap<T> {

	private HashMap<SpatialBucketPosition, Bucket<T>> map = new HashMap<SpatialBucketPosition, Bucket<T>>(256);
	private SpatialBucketPosition flyweight = new SpatialBucketPosition(0, 0);
	private int cellSize;
	
	public SpatialHashingMap(int worldSize, int expectedUnitCount) {
		cellSize = GameUtil.optimalCellSize(worldSize, expectedUnitCount);
	}
	
	public void insert(Vector2 v, T e) {
		getBucket(cell(v.x), cell(v.y)).add(e);
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
		flyweight.x = cx;
		flyweight.y = cy;
		Bucket<T> bucket = map.get(flyweight);
		if(bucket == null) {
			map.put(new SpatialBucketPosition(cx, cy), bucket = new Bucket<T>());
		}
		return bucket;
	}
	
	public Bucket<T> getBucketsMerged(Rectangle r) {
		Bucket<T> b = new Bucket<T>(4);
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				b.addAll(getBucket(x, y));
			}
		}
		return b;
	}
	
	public Bag<Bucket<T>> getBuckets(Rectangle r) {
		Bag<Bucket<T>> bag = new Bag<Bucket<T>>(1);
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				bag.add(getBucket(x, y));
			}
		}
		return bag;
	}
	
	private boolean hasBucket(int cx, int cy) {
		flyweight.x = cx;
		flyweight.y = cy;
		return map.containsKey(flyweight);
	}
	
	public void clear() {
		Collection<Bucket<T>> buckets = map.values();
		for (Iterator<Bucket<T>> iterator = buckets.iterator(); iterator.hasNext();) {
			iterator.next().clear();
		}
	}
		
	private int cell(float p) {
		if(p >= 0)
			return (int)(p/cellSize);
		else
			return (int)(-p/cellSize);
	}
	
	private class SpatialBucketPosition{
		
		private int x,y;
		
		public SpatialBucketPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode() {
			 return (x*71)^y;
		}
		
		@Override
		public boolean equals(Object obj) {
			SpatialBucketPosition pos = (SpatialBucketPosition)obj;
			return (pos.x == x && pos.y == y);
		}
	}
}
