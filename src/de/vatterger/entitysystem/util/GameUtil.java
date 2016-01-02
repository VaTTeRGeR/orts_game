package de.vatterger.entitysystem.util;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

public final class GameUtil {

	private GameUtil() {}

	public static float clamp(final float min, final float value, final float max){
		if(value > max)
			return max;
		else if(value > min)
			return value;
		else
			return min;
	}
	
	public static int clamp(final int min, final int value, final int max){
		if(value > max)
			return max;
		else if(value > min)
			return value;
		else
			return min;
	}
	
	public static float min(final float v1, final float v2){
		if(v1 > v2)
			return v2;
		else
			return v1;
	}
	
	public static int min(final int v1, final int v2){
		if(v1 > v2)
			return v2;
		else
			return v1;
	}
	
	public static float max(final float v1, final float v2){
		if(v1 < v2)
			return v2;
		else
			return v1;
	}

	public static int max(final int v1, final int v2){
		if(v1 < v2)
			return v2;
		else
			return v1;
	}
	
	public static int optimalCellSize(final int worldSize, final int expectedUnitCount){
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
	
	public static Rectangle circleToRectangle(Circle c){
		return circleToRectangle(c, new Rectangle());
	}

	public static Rectangle circleToRectangle(Circle c, Rectangle r){
		return r.set(c.x-c.radius, c.y-c.radius, 2*c.radius, 2*c.radius);
	}

	public static Circle rectangleToCircle(Rectangle r, boolean circleContainsRectangle){
		float radius;
		if(circleContainsRectangle)
			radius = max(r.height, r.width);
		else
			radius = min(r.height, r.width);
		return new Circle(r.x+radius, r.y+radius, radius);
	}
	
	public static Vector3 intersectMouseGroundPlane(Camera c, float x, float y){
		Vector3 v = new Vector3();
		Ray pickRay = c.getPickRay(x, y);
		Intersector.intersectRayPlane(pickRay, new Plane(Vector3.Z, 0f), v);
		return v;
	}

	public static void line(float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, ImmediateModeRenderer20 lineRenderer) {
		lineRenderer.color(r, g, b, a);
		lineRenderer.vertex(x1, y1, z1);
		lineRenderer.color(r, g, b, a);
		lineRenderer.vertex(x2, y2, z2);
	}

	public static void line(float x1, float y1, float z1, float x2, float y2, float z2, Color c, ImmediateModeRenderer20 imr20) {
		imr20.color(c.r, c.g, c.b, c.a);
		imr20.vertex(x1, y1, z1);
		imr20.color(c.r, c.g, c.b, c.a);
		imr20.vertex(x2, y2, z2);
	}
	
	public static void aabb(Rectangle rect, Color color, ImmediateModeRenderer20 imr20) {
		aabb(rect , 0f, color, imr20);
	}

	public static void aabb(Rectangle rect, float height, Color color, ImmediateModeRenderer20 imr20) {
		line(rect.x, rect.y, height,/**/rect.x+rect.width, rect.y, height,/**/color, imr20);
		line(rect.x, rect.y, height,/**/rect.x, rect.y+rect.height, height,/**/color, imr20);
		line(rect.x+rect.width, rect.y, height,/**/rect.x+rect.width, rect.y+rect.height, height,/**/color, imr20);
		line(rect.x, rect.y+rect.height, height,/**/rect.x+rect.width, rect.y+rect.height, height,/**/color, imr20);
	}

	public static void line(Vector3 v1, Vector3 v2, Color c, ImmediateModeRenderer20 lineRenderer) {
		lineRenderer.color(c.r, c.g, c.b, c.a);
		lineRenderer.vertex(v1.x,v1.y,v1.z);
		lineRenderer.color(c.r, c.g, c.b, c.a);
		lineRenderer.vertex(v2.x,v2.y,v2.z);
	}

	public static void deactivateEntity(Entity e) {
		GridMapFlag gridMapFlag = e.getComponent(GridMapFlag.class);
		if(gridMapFlag != null) {
			gridMapFlag.flag.removeFlag(GridMapBitFlag.ACTIVE);
		}
		e.edit().add(new Inactive());
	}
	
	@SafeVarargs
	public static void stripComponentsExcept(Entity e, Class<? extends Component> ...exceptClazz) {
		EntityEdit ed = e.edit();
		Bag<Component> components = new Bag<Component>(8);
		for (int i = 0; i < components.size(); i++) {
			Component c = components.get(i);
			if(exceptClazz != null) {
				boolean remove = true;
				for (int j = 0; j < exceptClazz.length; j++) {
					if (exceptClazz[j].isInstance(c)) {
						remove = false;
					}
				}
				if (remove) {
					ed.remove(c);
				}
			} else {
				ed.remove(c);
			}
		}
	}
	
	public static void stripComponents(Entity e) {
		EntityEdit ed = e.edit();
		Bag<Component> components = new Bag<Component>(8);
		e.getComponents(components);
		for (int i = 0; i < components.size(); i++) {
			ed.remove(components.get(i));
		}
	}
	
	public static boolean hasComponent(Entity e, Class<? extends Component> clazz) {
		return e.getComponent(clazz) != null;
	}
}
