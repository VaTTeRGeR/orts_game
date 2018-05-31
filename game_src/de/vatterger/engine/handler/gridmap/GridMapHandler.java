package de.vatterger.engine.handler.gridmap;

import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.engine.util.GameUtil;

public class GridMapHandler {
	
	private static Bag<Bag<CategorizedBucket>> buckets;
	private static float cellSizeInv;
	private static float offsetX;
	private static float offsetY;
	private static Rectangle	flyWeightRectangle;
	private static Circle		flyWeightCircle;
	
	private GridMapHandler(){}
	
	private static int optimalCellSize(final int worldSize, final int expectedUnitCount){
		int maxSize;
		
		if(worldSize > 0)
			maxSize = worldSize;
		else
			maxSize = 10000;
		
		if(expectedUnitCount > 32)
			return GameUtil.clamp(8,(int)(16*16*((float)worldSize/(float)expectedUnitCount)),256);
		else
			return maxSize;
	}
	
	public static void init(int size, int entityCount) {
		init(GridMapHandler.optimalCellSize(size, entityCount));
	}
	
	public static void init(float cellSize) {
		if(cellSize > 0)
			GridMapHandler.cellSizeInv = 1f/cellSize;
		else
			GridMapHandler.cellSizeInv = 1f/32f;
		
		offsetX = offsetY = 0f;
		buckets = new Bag<Bag<CategorizedBucket>>(1);
		flyWeightRectangle = new Rectangle();
		flyWeightCircle = new Circle();
	}
	
	public static void setOffset(float offsetX, float offsetY) {
		GridMapHandler.offsetX = offsetX;
		GridMapHandler.offsetY = offsetY;
	}
	
	public static void insertPoint(Vector2 v, int e, int gf) {
		insertPoint(v.x, v.y , e, gf);
	}
	
	public static void insertPoint(float x, float y, int e, int gf) {
		getBucketByCellCoordinates(cellX(x), cellY(y)).add(e, gf);
	}
	
	public static void insertCircle(Circle c, int e, int gf) {
		insertRectangle(GameUtil.circleToRectangle(c, flyWeightRectangle), e, gf);
	}
	
	public static void insertCircle(float x, float y, float radius, int e, int gf) {
		flyWeightCircle.set(x, y, radius);
		GameUtil.circleToRectangle(flyWeightCircle, flyWeightRectangle);
		insertRectangle(flyWeightRectangle, e, gf);
	}
	
	public static void insertRectangle(Rectangle r, int e, int gf) {
		final int startX = cellX(r.x), endX = cellX(r.x+r.width);
		final int startY = cellY(r.y), endY = cellY(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucketByCellCoordinates(x, y).add(e, gf);
			}
		}
	}
	
	public static CategorizedBucket getBucketByWorldCoordinates(float wx, float wy){
		return getBucketByCellCoordinates(cellX(wx), cellY(wy));
	}

	private static CategorizedBucket getBucketByCellCoordinates(int cx, int cy){
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
	
	public static IntBag getEntities(int gf, Circle c, IntBag fillBag) {
		return getEntities(gf, GameUtil.circleToRectangle(c, flyWeightRectangle), fillBag);
	}
	
	public static IntBag getEntities(int gf, Rectangle r, IntBag fillBag) {
		int startX = cellX(r.x), endX = cellX(r.x+r.width);
		int startY = cellY(r.y), endY = cellY(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucketByCellCoordinates(x, y).getAllWithSimilarFlag(gf, fillBag);
			}
		}
		return fillBag;
	}
	
	public static void clear() {
		int sx = buckets.size(), sy;
		CategorizedBucket bucket = null;
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
	
	private static int cellX(float p) {
		p -= offsetX;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}

	private static int cellY(float p) {
		p -= offsetY;
		return (int)(p > 0f ? p*cellSizeInv : 0f);
	}
}
