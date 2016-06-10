package de.vatterger.techdemo.handler.gridmap;

import com.badlogic.gdx.utils.StringBuilder;

final public class GridMapBitFlag {
	
	/*Entity is networked*/
	public static final long NETWORKED	= 	1;
	/*Entity has a collision component*/
	public static final long COLLISION	= 	2;
	/*Entity is immobile and does not act*/
	public static final long STATIC		= 	4;
	/*Entity has AI functionality*/
	public static final long AI			=	8;
	/*Entity is */
	public static final long ACTIVE		=	16;
		
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

	public GridMapBitFlag setFlag(long flagValue) {
		flag = flag | flagValue;
		return this;
	}
	
	public GridMapBitFlag removeFlag(long flagValue) {
		flag = flag & (~flagValue);
		return this;
	}
	
	public boolean isContaining(long flagValue) {
		return (flag & flagValue) == flagValue;
	}
	
	public boolean isEqual(long flagValue) {
		return (flag & flagValue) == flag;
	}
	
	public boolean isSubSetOf(long flagValue) {
		return (flag & flagValue) > 0;
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
