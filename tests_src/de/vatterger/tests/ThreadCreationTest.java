package de.vatterger.tests;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class ThreadCreationTest {
  public static void main(String[] args)
      throws InterruptedException {
    final AtomicInteger threads_created = new AtomicInteger(0);
    while (true) {
      final CountDownLatch latch = new CountDownLatch(1);
      new Thread() {
        { start(); }
        public void run() {
          latch.countDown();
          synchronized (this) {
            System.out.println("threads created: " +
                threads_created.incrementAndGet());
            try {
              wait();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        }
      };
      latch.await();
    }
  }
}