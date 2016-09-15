package de.vatterger.engine.handler.gridmap;

import com.badlogic.gdx.utils.StringBuilder;

final public class GridMapBitFlag {
	
	/*Entity is networked*/
	public static final long NETWORKED	= 	nextFlag();
	/*Entity has a collision component*/
	public static final long COLLISION	= 	nextFlag();
	/*Entity is immobile and does not act*/
	public static final long STATIC		= 	nextFlag();
	/*Entity has AI functionality*/
	public static final long AI			=	nextFlag();
	/*Entity is */
	public static final long ACTIVE		=	nextFlag();
		
	private static int flagCounter = 0;
	private static long nextFlag(){
		if(flagCounter > 63)
			throw new IllegalStateException("More than 64 flags have been specified.");
		return 1L<<flagCounter++;
	}
	
	private long flag;

	public GridMapBitFlag() {
		flag = 0;
	}
	
	public GridMapBitFlag(long initialFlagValue) {
		flag = initialFlagValue;
	}
	
	public GridMapBitFlag(GridMapBitFlag copyFrom) {
		flag = copyFrom.flag;
	}
	
	public long flagValue() {
		return flag;
	}

	public GridMapBitFlag setBit(int bit) {
		setFlag(1L<<bit);
		return this;
	}
	
	public GridMapBitFlag setFlag(long flagValue) {
		flag = flag | flagValue;
		return this;
	}
	
	public GridMapBitFlag removeBit(int bit) {
		removeFlag(1L<<bit);
		return this;
	}
	
	public GridMapBitFlag removeFlag(long flagValue) {
		flag = flag & (~flagValue);
		return this;
	}
	
	public boolean isBitSet(int bit) {
		return isContaining(1L<<bit);
	}
	
	public boolean isContaining(long flagValue) {
		return (flag & flagValue) == flagValue;
	}
	
	public boolean isEqual(long flagValue) {
		return (flag & flagValue) == flag && (flag & flagValue) == flagValue;
	}
	
	public boolean isSubSetOf(long flagValue) {
		return (flag & flagValue) != 0;
	}
	
	public GridMapBitFlag set(long flagValue) {
		flag = flagValue;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof GridMapBitFlag && isEqual(((GridMapBitFlag)o).flag);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("{").append(Long.toBinaryString(flag)).append("}").toString();
	}
}
