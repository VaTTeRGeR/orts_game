package de.vatterger.engine.handler.gridmap;

import com.badlogic.gdx.utils.StringBuilder;

final public class GridMapFlag {
	
	/*Entity is networked*/
	public static final int NETWORKED	= 	nextFlag();
	/*Entity has a collision component*/
	public static final int COLLISION	= 	nextFlag();
	/*Entity is immobile and does not act*/
	public static final int STATIC		= 	nextFlag();
	/*Entity has AI functionality*/
	public static final int AI				=	nextFlag();
	/*Entity is ALIVE*/
	public static final int ALIVE			=	nextFlag();
	
	/*Number of flag bits in use*/
	private static int FLAG_COUNTER = 0;
	
	private static int nextFlag() {
		if(FLAG_COUNTER >= 31)
			throw new IllegalStateException("More than 31 flags have been specified.");
		return 1<<(FLAG_COUNTER++);
	}
	
	private GridMapFlag() {}
	
	public static int setBit(int gf, int bit) {
		return setFlag(gf, 1<<bit);
	}
	
	public static int setFlag(int gf, int flagValue) {
		return gf | flagValue;
	}
	
	public static int removeBit(int gf, int bit) {
		return removeFlag(gf, 1<<bit);
	}
	
	public static int removeFlag(int gf, int flagValue) {
		return gf & (~flagValue);
	}
	
	public static boolean isBitSet(int gf, int bit) {
		return isContaining(gf, 1<<bit);
	}
	
	public static boolean isContaining(int gf, int flagValue) {
		return (gf & flagValue) == flagValue;
	}
	
	public static boolean isSubSetOf(int gf, int flagValue) {
		return (gf & flagValue) != 0;
	}
	
	public static String toString(int gf) {
		return new StringBuilder().append("0b").append(Integer.toBinaryString(gf)).toString();
	}
}
