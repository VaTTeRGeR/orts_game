package de.vatterger.tests;

import de.vatterger.engine.util.UnsafeUtil;
import sun.misc.Unsafe;

public class UnsafeTest {

	public static void main(String[] args) {

		Unsafe u = UnsafeUtil.getUnsafe();
		
		System.out.println("Memory address size: " + u.addressSize());

		final int byteArrayBaseOffset = u.arrayBaseOffset(byte[].class);
		
		System.out.println("byte[].class base offset: " + byteArrayBaseOffset);
		
		byte[] bytes = new byte[2048];
		
		for (int i = 0; i < bytes.length - 7; i+=8) {
			System.out.println("Putting long at " + (byteArrayBaseOffset + i));
			u.putLong(bytes, byteArrayBaseOffset + i, 0xAA00AA );
		}
	}
}
