package de.vatterger.entitysystem.quadtreeservice;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Quadtree<T> {
	private Quadtree<T>[] childTrees;
	private Quadtree<T> parent;
	private Rectangle area;
	private Bag<T> content;
	private Bag<T> childContent;
	private int depth = 0, maxDepth = 0, splitSize = 1;
	private boolean subdivided = false;

	public Quadtree(Rectangle area, int maxDepth, int splitSize) {
		this(null, area);
		this.maxDepth = maxDepth;
		this.splitSize = splitSize;
	}
	
	@SuppressWarnings("unchecked")
	private Quadtree(Quadtree<T> parent, Rectangle area) {
		childTrees = new Quadtree[4];
		this.parent = parent;
		this.area = area;
		if(parent != null) {
			depth = parent.depth + 1;
			maxDepth = parent.maxDepth;
			splitSize = parent.splitSize;
		}
		content = new Bag<T>(splitSize);
		childContent = new Bag<T>(4*splitSize);
	}
	
	private void subdivide() {
		Vector2 midpoint = area.getCenter(new Vector2());
		float halfWidth = Math.abs(area.width / 2f), halfHeight = Math.abs(area.height / 2f);
		childTrees[0] = new Quadtree<T>(this, new Rectangle(midpoint.x, midpoint.y, -halfWidth, halfHeight));// U-L
		childTrees[1] = new Quadtree<T>(this, new Rectangle(midpoint.x, midpoint.y, halfWidth, halfHeight));// U-R
		childTrees[2] = new Quadtree<T>(this, new Rectangle(midpoint.x, midpoint.y, -halfWidth, -halfHeight));// L-L
		childTrees[3] = new Quadtree<T>(this, new Rectangle(midpoint.x, midpoint.y, halfWidth, -halfHeight));// L-R
		subdivided = true;
	}
	
	public Quadtree<T> insert(T obj, Rectangle rect) {
		if(depth < maxDepth && content.size() >= splitSize) {
			if(!subdivided) {
				subdivide();
			}
			for (int i = 0; i < childTrees.length; i++) {
				if(childTrees[i].area.contains(rect)){
					childContent.add(obj);
					return childTrees[i].insert(obj, rect);
				}
			}
		}
		content.add(obj);
		return this;
	}
	
	public Bag<T> get(Rectangle rect, Bag<T> fillBag) {
		fillBag.addAll(content);
		for (int i = 0; i < childTrees.length; i++) {
			if(childTrees[i].area.contains(rect)){
				return childTrees[i].get(rect, fillBag);
			} else if(childTrees[i].area.overlaps(rect)) {
				childTrees[i].get(rect, fillBag);
			}
		}
		return fillBag;
	}
	
	
	public Bag<T> get(Rectangle rect, Bag<T> fillBag, int maxDepth) {
		fillBag.addAll(content);
		if(depth >= maxDepth){
			fillBag.addAll(childContent);
			return fillBag;
		}
		for (int i = 0; i < childTrees.length; i++) {
			if(childTrees[i].area.contains(rect)){
				return childTrees[i].get(rect, fillBag, maxDepth);
			} else if(childTrees[i].area.overlaps(rect)) {
				childTrees[i].get(rect, fillBag, maxDepth);
			}
		}
		return fillBag;
	}
	
	public Quadtree<T> update(Rectangle rect, T obj) {
		if(!area.contains(rect) && parent != null) {
			content.remove(obj);
			Quadtree<T> possibleNode = parent;
			while(possibleNode != null && !possibleNode.area.contains(rect)){
				possibleNode.childContent.remove(obj);
				possibleNode = possibleNode.parent;
			}
			possibleNode.insert(obj, rect);
		}
		return this;
	}
	
	public void clear() {
		content.clear();
		childContent.clear();
		if(subdivided)
			for (int i = 0; i < childTrees.length; i++) {
				childTrees[i].clear();
			}
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	public int getSplitSize() {
		return splitSize;
	}
}
