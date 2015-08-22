package de.vatterger.entitysystem.tools;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

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
}
