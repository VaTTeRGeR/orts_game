package de.vatterger.entitysystem.tools;

public final class MathUtil {

	private MathUtil() {}

	public static float clamp(final float min, final float value, final float max){
		if(value > max)
			return max;
		else if(value > min)
			return value;
		else
			return min;
	}
}
