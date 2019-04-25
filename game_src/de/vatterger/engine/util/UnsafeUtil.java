package de.vatterger.engine.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class UnsafeUtil {

	private static final Unsafe unsafe;
	
	static {
		
		Field field = null;

		try {
		
			field = Unsafe.class.getDeclaredField("theUnsafe");

		} catch (Exception e) {
			System.err.println("Could not get Unsafe field: " + e.getMessage());
		}

		try {
			
			field.setAccessible(true);

		} catch (Exception e) {
			System.err.println("Could not set Unsafe field accessible: " + e.getMessage());
		}
		
		
		Unsafe tmpUnsafe = null;
		
		try {
			
			tmpUnsafe = (Unsafe)field.get(null);
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.err.println("Could not get Unsafe from field: " + e.getMessage());
		}
		
		unsafe = tmpUnsafe;
	}
	
	private UnsafeUtil () {}
	
	public static Unsafe getUnsafe() {
		return unsafe;
	}
}
