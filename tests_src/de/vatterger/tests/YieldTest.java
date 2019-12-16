package de.vatterger.tests;

public class YieldTest {

	public static void main(String[] args) {

		NormalThread t0 = new NormalThread();
		NormalThread t1 = new NormalThread();
		NormalThread t2 = new NormalThread();
		
		t0.start();
		t1.start();
		t2.start();
	}
	
	static class YieldThread extends Thread {
		@Override
		public void run() {
			while(true) {
				System.out.println("YieldThread-" + this.getId());
				Thread.yield();
			}
		}
	}

	static class NormalThread extends Thread {
		@Override
		public void run() {
			while(true) {
				System.out.println("NormalThread-" + this.getId());
			}
		}
	}

}
