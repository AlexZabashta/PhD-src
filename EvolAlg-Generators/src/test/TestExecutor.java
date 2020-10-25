package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import utils.BlockingThreadPoolExecutor;

public class TestExecutor {
    public static void main(String[] args) {
        int n = 10;

        ExecutorService executor = Executors.newFixedThreadPool(n);
        // ExecutorService executor = new BlockingThreadPoolExecutor(n, false);
        for (int i = 0; i < 50; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " start");
                    System.out.flush();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(Thread.currentThread().getName() + " end");
                    System.out.flush();
                }
            });
        }

        executor.shutdown();

        System.out.println("Main wait");
        System.out.flush();
        try {
            executor.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main end");
        System.out.flush();

    }
}
