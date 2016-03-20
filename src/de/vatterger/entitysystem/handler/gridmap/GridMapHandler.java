package de.vatterger.entitysystem.handler.gridmap;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.application.GameConstants;
import de.vatterger.entitysystem.util.GameUtil;

public class GridMapHandler {

	private static Bag<Bag<CategorizedBucket>> buckets;
	private static int cellSize;
	private static Rectangle flyWeightRectangle;

	static {
		init(GameConstants.GRIDMAP_CELLSIZE);
	}

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
	
	public static void init(int expectedSize, int expectedEntityCount) {
		GridMapHandler.cellSize = GridMapHandler.optimalCellSize(expectedSize, expectedEntityCount);
		buckets = new Bag<Bag<CategorizedBucket>>(1);
		flyWeightRectangle = new Rectangle();
	}

	public static void init(int cellSize) {
		if(cellSize > 0)
			GridMapHandler.cellSize = cellSize;
		else
			GridMapHandler.cellSize = 32;
		buckets = new Bag<Bag<CategorizedBucket>>(1);
		flyWeightRectangle = new Rectangle();
	}

	public static void insert(Vector2 v, Integer e, GridMapBitFlag gf) {
		getBucketByCellCoordinates(cell(v.x), cell(v.y)).add(e, gf);
	}

	public static void insert(Circle c, Integer e, GridMapBitFlag gf) {
		insert(GameUtil.circleToRectangle(c, flyWeightRectangle), e, gf);
	}

	public static void insert(Rectangle r, Integer e, GridMapBitFlag gf) {
		final int startX = cell(r.x), endX = cell(r.x+r.width);
		final int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucketByCellCoordinates(x, y).add(e, gf);
			}
		}
	}

	public static CategorizedBucket getBucketByWorldCoordinates(float wx, float wy){
		return getBucketByCellCoordinates(cell(wx),cell(wy));
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

	public static Bag<Integer> getEntities(GridMapBitFlag gf, Circle c, Bag<Integer> fillBag) {
		return getEntities(gf, GameUtil.circleToRectangle(c, flyWeightRectangle), fillBag);
	}
	
	public static Bag<Integer> getEntities(GridMapBitFlag gf, Rectangle r, Bag<Integer> fillBag) {
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
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
		
	private static int cell(float p) {
		return (int)(p >= 0 ? p/cellSize : 0);
	}
}
