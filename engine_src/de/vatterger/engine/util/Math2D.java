package de.vatterger.engine.util;

public class Math2D {
	private Math2D(){}
	public static int angleToIndex(float angle){
		return (int)((angle*8f/360f)%8f);
	}
	
	public static float indexToAngle(int index){
		return (float)((index*360/8)%360);
	}
	
	public static float roundAngleEight(float angle) {
		return indexToAngle(angleToIndex(angle));
	}
	
	public static float round(float value, float rounding) {
		return Math.round(value*rounding)/rounding;
	}
}
