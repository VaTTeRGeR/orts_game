package de.vatterger.entitysystem.gridmapservice;

import com.artemis.Entity;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.tools.GameConstants;
import de.vatterger.entitysystem.tools.GameUtil;

public class GridMapService {

	private static Bag<Bag<CategorizedBucket<Entity>>> buckets = new Bag<Bag<CategorizedBucket<Entity>>>();
	private static int cellSize;
	private static Rectangle flyWeightRectangle = new Rectangle();
	
	static {
		cellSize = GameUtil.optimalCellSize(GameConstants.XY_BOUNDS, GameConstants.EXPECTED_ENTITYCOUNT);
	}
	
	private GridMapService(){}
	
	public static void insert(Vector2 v, Entity e, GridFlag gf) {
		getBucket(cell(v.x), cell(v.y)).add(e, gf);
	}

	public static void insert(Circle c, Entity e, GridFlag gf) {
		insert(GameUtil.circleToRectangle(c, flyWeightRectangle), e, gf);
	}

	public static void insert(Rectangle r, Entity e, GridFlag gf) {
		final int startX = cell(r.x), endX = cell(r.x+r.width);
		final int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucket(x, y).add(e, gf);
			}
		}
	}

	public static CategorizedBucket<Entity> getBucket(float wx, float wy){
		return getBucket(cell(wx),cell(wy));
	}
	
	private static CategorizedBucket<Entity> getBucket(int cx, int cy){
		Bag<CategorizedBucket<Entity>> bbx = buckets.safeGet(cx);
		if(bbx == null) {
			buckets.set(cx, bbx = new Bag<CategorizedBucket<Entity>>(1));
		}
		CategorizedBucket<Entity> by = bbx.safeGet(cy);
		if(by == null) {
			bbx.set(cy, by = new CategorizedBucket<Entity>());
		}
		return by;
	}

	public static Bag<Entity> getEntities(GridFlag gf, Rectangle r, Bag<Entity> fillBag) {
		int startX = cell(r.x), endX = cell(r.x+r.width);
		int startY = cell(r.y), endY = cell(r.y+r.height);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				getBucket(x, y).getAllWithSimilarFlag(gf, fillBag);
			}
		}
		return fillBag;
	}
	
	public static void clear() {
		int sx = buckets.size(), sy;
		CategorizedBucket<Entity> bucket = null;
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
		return (int)(p >= 0 ? p/cellSize : p/-cellSize);
	}
}
