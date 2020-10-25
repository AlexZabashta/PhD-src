package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class BlockingThreadPoolExecutor extends AbstractExecutorService {

    private boolean isShutdown;
    private final SynchronousQueue<Runnable> queue;
    private final Thread[] threads;

    public BlockingThreadPoolExecutor(int n, boolean fair) {
        this.threads = new Thread[n];
        this.queue = new SynchronousQueue<>(fair);

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    while (isShutdown == false) {
                        Runnable runnable;
                        try {
                            runnable = queue.poll(1, TimeUnit.SECONDS);
                            if (runnable != null) {
                                try {
                                    runnable.run();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            threads[i].start();
        }
    }

    @Override
    public boolean awaitTermination(long duration, TimeUnit timeUnit) throws InterruptedException {
        synchronized (threads) {
            if (isTerminated()) {
                return true;
            }
            long millis = timeUnit.toMillis(duration);
            for (Thread thread : threads) {
                if (millis <= 0) {
                    return false;
                }
                long start = System.currentTimeMillis();
                thread.join(millis);
                long finish = System.currentTimeMillis();
                millis -= finish - start;
            }

            return millis > 0;
        }
    }

    @Override
    public void execute(Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (isShutdown()) {
            throw new IllegalStateException("Executor service is shutdown");
        }
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isShutdown() {
        synchronized (threads) {
            return isShutdown;
        }
    }

    @Override
    public boolean isTerminated() {
        synchronized (threads) {
            if (!isShutdown) {
                return false;
            }
            for (Thread thread : threads) {
                if (!thread.isAlive()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void shutdown() {
        synchronized (threads) {
            isShutdown = true;
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> list = new ArrayList<>();
        Runnable runnable;
        while ((runnable = queue.poll()) != null) {
            list.add(runnable);
        }
        return list;
    }

    public Thread thread(int index) {
        synchronized (threads) {
            return threads[index];
        }
    }
}
