package de.vatterger.entitysystem.gridmapservice;

public class GridFlag {
	public static final int NETWORKED	= 	1,
							COLLISION	= 	2,
							AI			=	4;
	
	private int flag = 0;
	
	public GridFlag() {
	}
	
	public GridFlag(int initialFlag) {
		flag = initialFlag;
	}
	
	public int flag() {
		return flag;
	}

	public GridFlag addFlag(int f) {
		flag = flag | f;
		return this;
	}
	
	public GridFlag removeFlag(int f) {
		flag = flag & (~f);
		return this;
	}
	
	public boolean hasFlagsOf(int f) {
		return (flag & f) == f;
	}
	
	public boolean hasAllFlagsOf(int f) {
		return (flag & f) == flag;
	}
	
	public GridFlag setFlag(int f) {
		flag = f;
		return this;
	}

	public boolean isFlagSet(int pos) {
	   return (flag & (1 << pos)) != 0;
	}
}
