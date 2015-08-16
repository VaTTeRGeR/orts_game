package de.vatterger.entitysystem.tools;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class SpatialPartitionMap<T> {

	private Bag<Bag<Bucket<T>>> buckets = new Bag<Bag<Bucket<T>>>();
	private int cellSize, maxSize;
	
	public SpatialPartitionMap(int worldSize, int expectedUnitCount) {
		cellSize = optimalCellSize(worldSize, expectedUnitCount);
		System.out.println("CELLSIZE: "+cellSize);
	}
	
	private int optimalCellSize(final int worldSize, final int expectedUnitCount){
		int maxSize;
		if(worldSize > 0)
			maxSize = worldSize;
		else
			maxSize = 10000;
		if(expectedUnitCount > 32)
			return GameUtil.clamp(2,(int)(16*16*((float)worldSize/(float)expectedUnitCount)),256);
		else
			return maxSize;
	}
	
	public void insert(Vector3 v, T e) {
		getBucket(cell(v.x), cell(v.y)).add(e);
	}

	@Deprecated
	public void insertFast(Vector3 v, T e) {
		getBucketFast(cell(v.x), cell(v.y)).add(e);
	}

	public void insert(Circle c, T e) {
		insert(GameUtil.circleToRectangle(c), e);
	}

	@Deprecated
	public void insertFast(Circle c, T e) {
		insertFast(GameUtil.circleToRectangle(c), e);
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

	@Deprecated
	public void insertFast(Rectangle r, T e) {
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucketFast(x, y).add(e);
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
			buckets.get(cx).set(cy, new Bucket<T>());
		}
		return buckets.get(cx).get(cy);
	}
	
	@Deprecated
	public Bucket<T> getBucketFast(float wx, float wy){
		return getBucketFast(cell(wx),cell(wy));
	}

	@Deprecated
	public Bucket<T> getBucketFast(int cx, int cy) {
		return buckets.get(cx).get(cy);
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

	@Deprecated
	public Bucket<T> getBucketsMergedFast(Rectangle r) {
		Bucket<T> b = new Bucket<T>(4);
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				b.addAll(getBucketFast(x, y));
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

	@Deprecated
	public Bag<Bucket<T>> getBucketsFast(Rectangle r) {
		Bag<Bucket<T>> bag = new Bag<Bucket<T>>(1);
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				try {
				bag.add(getBucketFast(x, y));}catch(Exception e){
					System.out.println(r.x+","+r.y+" : "+e.toString());
				}
			}
		}
		return bag;
	}
	
	private boolean hasBucket(int x, int y) {
		return (buckets.get(x) != null && buckets.get(x).get(y) != null);
	}
	
	public void clear() {
		for (int x = 0; x < buckets.size(); x++) {
			if(buckets.get(x) == null)
				continue;
			for (int y = 0; y < buckets.get(x).size(); y++) {
				if(buckets.get(x).get(y) != null) {
					buckets.get(x).get(y).clear();
				}
			}
		}
	}
		
	public void clearFast() {
		for (int x = 0; x < buckets.size(); x++) {
			for (int y = 0; y < buckets.get(x).size(); y++) {
				buckets.get(x).get(y).clear();
			}
		}
	}
		
	private int cell(float p) {
		if(p >= 0)
			return (int)(p/cellSize);
		else
			return (int)(-p/cellSize);
	}
}
