package de.vatterger.tests;

import java.io.IOException;
import java.util.ArrayList;

public class MultiThreadFibonacci {

    static ArrayList<Thread> al = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        al.add(new Thread(() -> fib1(47)));
        al.add(new Thread(() -> fib2(47)));

        for (Thread t : al)
            t.start();
        for (Thread t: al)
            t.join();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(totalTime);
    }

    public static int fib1(int x) {
        return x <= 2 ? 1 : fib1(x-2) + fib1(x-1);
    }

    public static int fib2(int x) {
        return x <= 2 ? 1 : fib2(x-2) + fib2(x-1);
    }

}