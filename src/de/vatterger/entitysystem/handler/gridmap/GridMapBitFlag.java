package de.vatterger.entitysystem.handler.gridmap;

import com.badlogic.gdx.utils.StringBuilder;

public class GridMapBitFlag {
	public static final long NETWORKED	= 	1,
							COLLISION	= 	2,
							STATIC		= 	4,
							AI			=	8,
							ACTIVE		=	16;
		
	private long flag;
	
	public GridMapBitFlag() {
		flag = 0;
	}
	
	public GridMapBitFlag(long initialFlagValue) {
		flag = initialFlagValue;
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
	
	public boolean isSuperSetOf(long flagValue) {
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
	public String toString() {
		return new StringBuilder().append("{").append(Long.toBinaryString(flag)).append("}").toString();
	}
}
