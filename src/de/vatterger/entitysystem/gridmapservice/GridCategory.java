package de.vatterger.entitysystem.gridmapservice;

public enum GridCategory {
	PHYSICS,
	AI,
	NETWORKED,
	NULL;
	
	private int flags = 0;
	
	private GridCategory(){
		flags = 1 << ordinal();
	}
	
	public int flagValue() {
		return flags;
	}

	public void addFlag(GridCategory g) {
		flags |= g.flags;
	}
	
	public boolean hasSomeFlagsOf(GridCategory g) {
		return (flags & g.flags) == flags;
	}
	
	public boolean hasAllFlagsOf(GridCategory g) {
		return (flags & g.flags) == g.flags;
	}
}
