package de.vatterger.tests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.util.Profiler;

public class ExecutorServiceTest {

	static int n = 0;
	static float nx = 0;
	
	public static void main(String[] args) {
		ExecutorService ex = Executors.newFixedThreadPool(4);
		Profiler p = new Profiler("ThreadPool", TimeUnit.MILLISECONDS);
		for(int i = 0; i < 10000; i++) {
			ex.execute(new ExecutorServiceTest.Task());
		}

		ex.shutdown();
		try {
			ex.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		p.log();
	}

	private static class Task implements Runnable {
		private float x = n++;
		@Override
		public void run() {
			int rounds = 100000;
			for (int i = 0; i < rounds; i++) {
				x = MathUtils.sin(x);
			}
			nx += x;
		}
	}
}
