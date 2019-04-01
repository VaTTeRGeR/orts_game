package de.vatterger.tests;

public class TestLWJGLSleep {

	public static void main(String[] args) throws InterruptedException {
		
		long previousTime = System.nanoTime();
		
		while (true) {
			
			Sync.sync(60);
			//Thread.sleep(1000/60);
			
			long time = System.nanoTime();
			
			System.out.println( ( time-previousTime) / 1000 + " us");
			
			previousTime = time;
		}
	}

}
