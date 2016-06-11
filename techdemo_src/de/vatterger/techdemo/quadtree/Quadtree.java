package de.vatterger.techdemo.quadtree;

import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.engine.util.GameUtil;

public class Quadtree<T> {
	private Rectangle area;

	private Quadtree<T>[]	childTrees;
	private Quadtree<T>		parent;
	
	private Bag<SpatialEntry<T>>	content;
	private Bag<SpatialEntry<T>>	childContent;
	
	private int depth = 0, maxDepth = 0, splitSize = 1;
	
	private boolean subdivided = false;

	public Quadtree(Rectangle area, int maxDepth, int splitSize) {
		this(null, area);
		if(splitSize > 1)
			this.splitSize = splitSize;
		if(maxDepth > 0)
			this.maxDepth = maxDepth;
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
		content = new Bag<SpatialEntry<T>>(splitSize);
		childContent = new Bag<SpatialEntry<T>>(4*splitSize);
	}
	
	private void subdivide() {
		Vector2 midpoint = area.getCenter(new Vector2());
		float halfWidth = Math.abs(area.width / 2f), halfHeight = Math.abs(area.height / 2f);
		
		childTrees[0] = new Quadtree<T>(this, new Rectangle(midpoint.x-halfWidth, midpoint.y			, halfWidth, halfHeight));// U-L
		childTrees[1] = new Quadtree<T>(this, new Rectangle(midpoint.x			, midpoint.y			, halfWidth, halfHeight));// U-R
		childTrees[2] = new Quadtree<T>(this, new Rectangle(midpoint.x-halfWidth, midpoint.y-halfHeight	, halfWidth, halfHeight));// L-L
		childTrees[3] = new Quadtree<T>(this, new Rectangle(midpoint.x			, midpoint.y-halfHeight	, halfWidth, halfHeight));// L-R

		subdivided = true;
		
		Bag<SpatialEntry<T>> oldContent = content;
		content = new Bag<SpatialEntry<T>>(splitSize);
		childContent.clear();
		for (int i = 0; i < oldContent.size(); i++) {
			insert(oldContent.get(i));
		}
	}
	
	public Quadtree<T> insert(SpatialEntry<T> se) {
		if(subdivided || (depth < maxDepth &&  splitSize <= content.size())) {
			
			if(!subdivided)
				subdivide();
			
			for (int i = 0; i < childTrees.length; i++) {
				if(childTrees[i].area.contains(se.getSpatial())) {
					childContent.add(se);
					return childTrees[i].insert(se);
				}
			}
		}
		
		content.add(se);
		
		return this;
	}
	
	public Bag<SpatialEntry<T>> get(Rectangle rect, Bag<SpatialEntry<T>> fillBag, boolean optimizeLargeArea) {
		fillBag.addAll(content);
		if (subdivided) {
			if (optimizeLargeArea && rect.area() >= area.area()) {
				return getAllChildren(fillBag);
			} else {
				for (int i = 0; i < childTrees.length; i++) {
					if (childTrees[i].area.contains(rect)) {
						return childTrees[i].get(rect, fillBag, optimizeLargeArea);
					} else if (childTrees[i].area.overlaps(rect)) {
						childTrees[i].get(rect, fillBag, optimizeLargeArea);
					}
				}
			}
		}
		return fillBag;
	}

	public Bag<SpatialEntry<T>> getAll(Bag<SpatialEntry<T>> fillBag) {
		fillBag.addAll(content);
		return getAllChildren(fillBag);
	}
	
	public Bag<SpatialEntry<T>> getAllChildren(Bag<SpatialEntry<T>> fillBag) {
		fillBag.addAll(childContent);
		return fillBag;
	}

	public Quadtree<T> update(SpatialEntry<T> se) {
		if (area.contains(se.getSpatial()) && depth == maxDepth) {
			content.remove(se);
			return insert(se);
		} else if (parent != null) {
			int index = content.indexOf(se);
			content.remove(index);

			Quadtree<T> possibleNode = parent;
			while (possibleNode != null && !possibleNode.area.contains(se.getSpatial())) {
				possibleNode.childContent.remove(se);
				possibleNode = possibleNode.parent;
			}
			possibleNode.insert(se);
			return possibleNode;
		}
		return this;
	}
	
	public void clear() {
		clear(true);
	}
	
	public void clear(boolean recursive) {
		content.clear();
		if(subdivided && recursive && childContent.size() > 0) {
			childContent.clear();
			for (int i = 0; i < childTrees.length; i++) {
				childTrees[i].clear();
			}
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
	
	public void render(ImmediateModeRenderer20 imr20) {
		Color sizeColor;
		if (content.isEmpty())
			sizeColor = Color.WHITE;
		else if (content.size() < splitSize)
			sizeColor = Color.GREEN;
		else
			sizeColor = Color.RED;

		if (subdivided && childContent.size() > 0) {
			for (int i = 0; i < childTrees.length; i++) {
				childTrees[i].render(imr20);
			}
		} else {
			GameUtil.aabb(area, 0f, sizeColor, imr20);
			if (content.size() > 0) {
				for (int i = 0; i < content.size(); i++) {
					GameUtil.aabb(content.get(i).getSpatial(), 0f, Color.GRAY, imr20);
				}
			}
		}
		if (imr20.getNumVertices() > imr20.getMaxVertices() * 0.75f)
			imr20.flush();
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(area.toString()).append("|D").append(depth).append("|C").append(content.size()).append("|CC").append(childContent.size()).toString();
	}
}