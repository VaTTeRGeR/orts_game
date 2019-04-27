package de.vatterger.tests;

import java.nio.ByteOrder;

import de.vatterger.engine.util.UnsafeUtil;
import sun.misc.Unsafe;

public class UnsafeArrayCopyTest {

	public static void main(String[] args) throws InterruptedException {

		if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
		  System.out.println("Big-endian");
		} else {
		  System.out.println("Little-endian");
		}
		
		Thread.sleep(2000);
		
		while(true) {
			
			runTest(true);
			
			Thread.sleep(100);
		}
	}
	
	private static void runTest(boolean useUnsafe) {

		Unsafe u = UnsafeUtil.getUnsafe();

		final int byteArrayBaseOffset = u.arrayBaseOffset(byte[].class);
		final int intArrayBaseOffset = u.arrayBaseOffset(int[].class);
		
		final long ARRAY_SIZE = 512;
		
		byte[]	bytes	= new byte[(int)ARRAY_SIZE * 4];
		int[]	ints	= new int[(int)ARRAY_SIZE];
		
		for (int i = 0; i < ints.length; i++) {
			ints[i] = i;
		}
		
		final long TEST_ITERATIONS = 32*1024;
		
		final long tStart = System.nanoTime();
		
		if(useUnsafe) {
			
			for (int i = 0; i < TEST_ITERATIONS; i++) {
				u.copyMemory(ints, intArrayBaseOffset, bytes, byteArrayBaseOffset, ARRAY_SIZE * 4);
			}
			
		} else {
			
			for (int i = 0; i < TEST_ITERATIONS; i++) {
				
				int intsOffset = 0;
				
				for (int j = 0; j < bytes.length - 3; j += 4) {
					
					int value = ints[intsOffset++];
					
					bytes[j + 0] = (byte)(value << 0);
					bytes[j + 1] = (byte)(value << 8);
					bytes[j + 2] = (byte)(value << 16);
					bytes[j + 3] = (byte)(value << 24);
				}
			}
		}
		
		final long tDelta = System.nanoTime() - tStart;
		
		System.out.println("Took " + tDelta/TEST_ITERATIONS + " ns for one int["+ARRAY_SIZE+"] copy");
		System.out.println("Copy rate " + (TEST_ITERATIONS * ARRAY_SIZE * 1000L * 1000L * 1000L / 1024L / 1024L / tDelta) + " MB/s int copy");
	}
}
