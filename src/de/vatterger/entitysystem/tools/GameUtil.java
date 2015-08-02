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
	
	public static float min(final float v1, final float v2){
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
	
	public static Rectangle circleToRectangle(Circle c){
		return new Rectangle(c.x-c.radius, c.y-c.radius, 2*c.radius, 2*c.radius);
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
