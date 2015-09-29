package de.vatterger.entitysystem.quadtreeservice;

import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.Main;
import de.vatterger.entitysystem.util.GameUtil;

public class Quadtree<T> {
	private Quadtree<T>[] childTrees;
	private Quadtree<T> parent;
	private Rectangle area;
	private Bag<T> content;
	private Bag<Rectangle> contentPositions;
	private Bag<T> childContent;
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
		content = new Bag<T>(splitSize);
		contentPositions = new Bag<Rectangle>(splitSize);
		childContent = new Bag<T>(4*splitSize);
	}
	
	private void subdivide() {
		Main.printConsole("Subdividing "+this);
		
		Vector2 midpoint = area.getCenter(new Vector2());
		float halfWidth = Math.abs(area.width / 2f), halfHeight = Math.abs(area.height / 2f);
		
		childTrees[0] = new Quadtree<T>(this, new Rectangle(midpoint.x-halfWidth, midpoint.y			, halfWidth	, halfHeight));// U-L
		childTrees[1] = new Quadtree<T>(this, new Rectangle(midpoint.x			, midpoint.y			, halfWidth, halfHeight));// U-R
		childTrees[2] = new Quadtree<T>(this, new Rectangle(midpoint.x-halfWidth, midpoint.y-halfHeight	, halfWidth, halfHeight));// L-L
		childTrees[3] = new Quadtree<T>(this, new Rectangle(midpoint.x			, midpoint.y-halfHeight	, halfWidth	, halfHeight));// L-R

		subdivided = true;
		
		Main.printConsole("Reinserting "+content.size()+" elements");

		Bag<T> oldContent = new Bag<T>(content.size());
		Bag<Rectangle> oldContentPositions = new Bag<Rectangle>(content.size());
		
		oldContent.addAll(content);
		content.clear();
		
		oldContentPositions.addAll(contentPositions);
		contentPositions.clear();
		
		for (int i = 0; i < oldContent.size(); i++) {
			insert(oldContent.get(i), oldContentPositions.get(i));
		}
		
		Main.printConsole("Subdivided "+this+" and reinserted "+oldContent.size()+" elements");
	}
	
	public Quadtree<T> insert(T obj, Rectangle rect) {
		Main.printConsole("Current Tree: "+toString());
		if(subdivided || (depth < maxDepth && content.size() >= splitSize)) {
			Main.printConsole("Trying to insert into subtree");
			if(!subdivided)
				subdivide();
			for (int i = 0; i < childTrees.length; i++) {
				if(childTrees[i].area.contains(rect)){
					Main.printConsole("passing "+obj.toString()+" to childtree "+childTrees[i].area);
					childContent.add(obj);
					return childTrees[i].insert(obj, rect);
				} else {
					Main.printConsole("Childtree "+childTrees[i]+" doesn't fit to "+rect);
				}
			}
		}
		Main.printConsole("inserting "+obj.toString()+" in "+this);
		Main.printConsole("---");
		contentPositions.add(rect);
		content.add(obj);
		return this;
	}
	
	public Bag<T> get(Rectangle rect, Bag<T> fillBag, boolean optimizeLargeArea) {
		fillBag.addAll(content);
		if (subdivided) {
			if (optimizeLargeArea && rect.contains(area)) {
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
	
	public Bag<T> get(Rectangle rect, Bag<T> fillBag) {
		return get(rect, fillBag, false);
	}

	public Bag<T> getAll(Bag<T> fillBag) {
		fillBag.addAll(content);
		return getAllChildren(fillBag);
	}
	
	public Bag<T> getAllChildren(Bag<T> fillBag) {
		fillBag.addAll(childContent);
		return fillBag;
	}
	
	public Quadtree<T> update(Rectangle rect, T obj) {
		if(subdivided && area.contains(rect)) {
			
			content.remove(obj);
			return insert(obj, rect);
			
		} else if(parent != null) {
			
			int index = content.indexOf(obj);
			content.remove(index);
			contentPositions.remove(index);

			Quadtree<T> possibleNode = parent;
			while(possibleNode != null && !possibleNode.area.contains(rect)){
				possibleNode.childContent.remove(obj);
				possibleNode = possibleNode.parent;
			}
			possibleNode.insert(obj, rect);
			return possibleNode;
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
	
	public void render(ImmediateModeRenderer20 imr20) {
		Color sizeColor;
		if(content.isEmpty())
			sizeColor = Color.WHITE;
		else if(content.size() < splitSize)
			sizeColor = Color.GREEN;
		else
			sizeColor = Color.RED;

		GameUtil.line(area.x+area.width/2f, area.y+area.height/2f, 0f, area.x+area.width/2f, area.y+area.height/2f, content.size()+1f, sizeColor, imr20);
		GameUtil.line(area.x, area.y, 0f,/**/area.x+area.width, area.y, 0f,/**/Color.YELLOW, imr20);
		GameUtil.line(area.x, area.y, 0f,/**/area.x, area.y+area.height, 0f,/**/Color.YELLOW, imr20);
		GameUtil.line(area.x+area.width, area.y, 0f,/**/area.x+area.width, area.y+area.height, 0f,/**/Color.YELLOW, imr20);
		GameUtil.line(area.x, area.y+area.height, 0f,/**/area.x+area.width, area.y+area.height, 0f,/**/Color.YELLOW, imr20);
		if(subdivided) {
			for (int i = 0; i < childTrees.length; i++) {
				childTrees[i].render(imr20);
			}
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(area.toString()).append("|D").append(depth).append("|C").append(content.size()).append("|CC").append(childContent.size()).toString();
	}
}
