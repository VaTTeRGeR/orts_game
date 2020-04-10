package de.vatterger.engine.handler.gridmap;

import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.engine.util.GameUtil;

public class GridMap2D {
	
	private final Bag<Bag<CategorizedBucket>> buckets;
	
	private final Rectangle	flyWeightRectangle;
	private final Circle		flyWeightCircle;
	
	private final float cellSizeInv;
	
	private final float offsetX;
	private final float offsetY;
	
	public GridMap2D(float cellSize) {
		this(cellSize, 0f, 0f);
	}
	
	public GridMap2D(int size, int entityCount, float offsetX, float offsetY) {
		this(GridMap2D.optimalCellSize(size, entityCount), offsetX, offsetY);
	}
	
	public GridMap2D(float cellSize, float offsetX, float offsetY) {
		
		System.out.println("Creating gridmap with cellsize: " + cellSize);
		
		if(cellSize > 0) {
			cellSizeInv = 1f/cellSize;
		} else {
			cellSizeInv = 1f/32f;
		}
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		buckets = new Bag<Bag<CategorizedBucket>>(1);
		
		flyWeightRectangle = new Rectangle();
		flyWeightCircle = new Circle();
	}
	
	private static int optimalCellSize(int worldSize, int expectedUnitCount) {
		
		if(worldSize <= 0) {
			throw new IllegalArgumentException("Parameter worldSize must be >= 0. worldSize: " + worldSize);
		}
		
		return GameUtil.clamp(8,(int)(16*16*((float)worldSize/(float)expectedUnitCount)), 256);
	}
	
	public void insertPoint(Vector2 v, int e, int gf) {
		insertPoint(v.x, v.y , e, gf);
	}
	
	public void insertPoint(float x, float y, int e, int gf) {
		getBucketByCellCoordinates(cellX(x), cellY(y)).add(e, gf);
	}
	
	public void insertCircle(Circle c, int e, int gf) {
		insertRectangle(GameUtil.circleToRectangle(c, flyWeightRectangle), e, gf);
	}
	
	public void insertCircle(float x, float y, float radius, int e, int gf) {
		
		flyWeightCircle.set(x, y, radius);
		
		GameUtil.circleToRectangle(flyWeightCircle, flyWeightRectangle);
		
		insertRectangle(flyWeightRectangle, e, gf);
	}
	
	public void insertRectangle(Rectangle r, int e, int gf) {
		
		final int startX = cellX(r.x), endX = cellX(r.x+r.width);
		final int startY = cellY(r.y), endY = cellY(r.y+r.height);
		
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucketByCellCoordinates(x, y).add(e, gf);
			}
		}
	}
	
	public CategorizedBucket getBucketByWorldCoordinates(float wx, float wy) {
		return getBucketByCellCoordinates(cellX(wx), cellY(wy));
	}

	private CategorizedBucket getBucketByCellCoordinates(int cx, int cy) {
		
		Bag<CategorizedBucket> bbx = buckets.safeGet(cx);
		
		if(bbx == null) {
			buckets.set(cx, bbx = new Bag<CategorizedBucket>(1));
		}
		
		CategorizedBucket by = bbx.safeGet(cy);
		
		if(by == null) {
			bbx.set(cy, by = new CategorizedBucket());
		}
		
		return by;
	}
	
	public IntBag getEntities(int gf, Circle c, IntBag fillBag) {
		return getEntities(gf, GameUtil.circleToRectangle(c, flyWeightRectangle), fillBag);
	}
	
	public IntBag getEntities(int gf, Rectangle r, IntBag fillBag) {
		
		int startX = cellX(r.x), endX = cellX(r.x+r.width);
		int startY = cellY(r.y), endY = cellY(r.y+r.height);
		
		for (int x = startX; x <= endX; x++) {
			
			for (int y = startY; y <= endY; y++) {
				getBucketByCellCoordinates(x, y).getAllWithSimilarFlag(gf, fillBag);
			}
		}
		
		return fillBag;
	}
	
	public void clear() {
		
		int sx = buckets.size();
		
		for (int x = 0; x < sx; x++) {
			
			Bag<CategorizedBucket> bucketBag = buckets.get(x);
			
			if (bucketBag == null) {
				continue;
				
			} else {
				
				int sy = bucketBag.size();
				
				for (int y = 0; y < sy; y++) {
					
					CategorizedBucket bucket = bucketBag.get(y);
					
					if (bucket != null) {
						bucket.clear();
					}
				}
			}
		}
	}
	
	private int cellX(float p) {
		p -= offsetX;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}

	private int cellY(float p) {
		p -= offsetY;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}
}
