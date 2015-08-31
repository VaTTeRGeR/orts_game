package de.vatterger.entitysystem.gridmapservice;

import com.artemis.Entity;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.tools.GameConstants;
import de.vatterger.entitysystem.tools.GameUtil;

public class GridMapService<T> {

	private static GridMapService<Entity> service;

	private Bag<Bag<CategorizedBucket<T>>> buckets = new Bag<Bag<CategorizedBucket<T>>>();
	private int cellSize;
	
	private GridMapService() {
		this(GameConstants.XY_BOUNDS, GameConstants.EXPECTED_ENTITYCOUNT);
	}
	
	private GridMapService(int worldSize, int expectedUnitCount) {
		cellSize = GameUtil.optimalCellSize(worldSize, expectedUnitCount);
	}
	
	public static synchronized GridMapService<Entity> instance() {
		if(!loaded())
			service = new GridMapService<Entity>();
		return service;
	}
	
	public static boolean loaded() {
		return service != null;
	}
	
	public void insert(Vector2 v, T e, GridFlag gf) {
		getBucket(cell(v.x), cell(v.y)).add(e, gf);;
	}

	public void insert(Circle c, T e, GridFlag gf) {
		insert(GameUtil.circleToRectangle(c), e, gf);
	}

	public void insert(Rectangle r, T e, GridFlag gf) {
		final int startX = cell(r.x), endX = cell(r.x+r.width);
		final int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucket(x, y).add(e, gf);
			}
		}
	}

	public CategorizedBucket<T> getBucket(float wx, float wy){
		return getBucket(cell(wx),cell(wy));
	}
	
	public CategorizedBucket<T> getBucket(int cx, int cy){
		Bag<CategorizedBucket<T>> bbx = buckets.safeGet(cx);
		if(bbx == null) {
			buckets.set(cx, bbx = new Bag<CategorizedBucket<T>>(1));
		}
		CategorizedBucket<T> by = bbx.safeGet(cy);
		if(by == null) {
			bbx.set(cy, by = new CategorizedBucket<T>());
		}
		return by;
	}

	public Bag<CategorizedBucket<T>> getBuckets(Rectangle r) {
		Bag<CategorizedBucket<T>> bag = new Bag<CategorizedBucket<T>>(8);
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
		CategorizedBucket<T> bucket = null;
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
		return (int)(p >= 0 ? p/cellSize : p/-cellSize);
	}
}
