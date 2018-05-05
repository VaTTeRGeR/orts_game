package de.vatterger.tests;

public class ByteBufferTest {
	public static void main(String[] args) throws InterruptedException {
		seqAGreaterB(1, 0);
		seqAGreaterB(0, 1);
		seqAGreaterB(30000, 255);
		seqAGreaterB(255, 30000);
		
		seqDif(Short.MAX_VALUE,0);
		seqDif(Short.MAX_VALUE-17000,Short.MAX_VALUE);
	}
	
	private static void seqDif(int A, int B) {
		System.out.print("A=" + A + " dif B=" + B + " = ");
		
		int d = A - B;
		if(d > Short.MAX_VALUE/2) {
			A -= Short.MAX_VALUE - 1;
		} else if(d < -Short.MAX_VALUE/2) {
			B -= Short.MAX_VALUE - 1;
		}
		System.out.println(Math.abs(A - B));
	}

	private static void seqAGreaterB(int A, int B) {
		System.out.print("A=" + A + " > B=" + B + " is ");
		boolean result =	( (A > B) && (A-B < Short.MAX_VALUE/2) ) ||
							( (A < B) && (B-A > Short.MAX_VALUE/2) );
		System.out.println(result);
	}

}
