package de.vatterger.entitysystem.gridmapservice;

import com.badlogic.gdx.utils.StringBuilder;

public class GridFlag {
	public static final long NETWORKED	= 	1,
							COLLISION	= 	2,
							STATIC		= 	4,
							AI			=	8,
							ACTIVE		=	16;
	
	public static final String[] FLAGNAMES = {"NETWORKED","COLLISION","STATIC","AI","ACTIVE"};
	
	private long flag;
	
	public GridFlag() {
		flag = 0;
	}
	
	public GridFlag(long initialFlagValue) {
		flag = initialFlagValue;
	}
	
	public long flagValue() {
		return flag;
	}

	public GridFlag setFlag(long flagValue) {
		flag = flag | flagValue;
		return this;
	}
	
	public GridFlag removeFlag(long flagValue) {
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
	
	public GridFlag set(long flagValue) {
		flag = flagValue;
		return this;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("{").append(Long.toBinaryString(flag)).append("}").toString();
	}
}
