package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercise 4 — Submit tasks to a fixed thread pool.
 *
 * Demonstrates how an ExecutorService reuses a bounded number
 * of worker threads to process many tasks.
 */
public class BatchTaskExecutor {

    public static final int POOL_SIZE = 3;

    /** Counts how many tasks have completed */
    private final AtomicInteger completedCount = new AtomicInteger(0);

    /**
     * Records the names of worker threads that executed tasks.
     * ArrayList is not thread-safe, so access must be synchronized.
     */
    private final List<String> workerNames = new ArrayList<>();

    /**
     * Process each name in {@code taskNames} using a fixed-size thread pool.
     */
    public void processBatch(List<String> taskNames) throws InterruptedException {

        // Create a fixed-size thread pool
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

        for (String name : taskNames) {
            pool.submit(() -> {

                // (1) Atomically record task completion
                completedCount.incrementAndGet();

                // (2) Record which worker thread processed the task
                synchronized (workerNames) {
                    workerNames.add(Thread.currentThread().getName());
                }
            });
        }

        // Stop accepting new tasks
        pool.shutdown();

        // Wait up to 10 seconds for all submitted tasks to finish
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    /** Total tasks that have completed. */
    public int getCompletedCount() {
        return completedCount.get();
    }

    /** Names of worker threads that ran tasks (may contain duplicates). */
    public List<String> getWorkerNames() {
        synchronized (workerNames) {
            return List.copyOf(workerNames);
        }
    }

    /** Number of distinct worker thread names (should be ≤ POOL_SIZE). */
    public long getDistinctWorkerCount() {
        synchronized (workerNames) {
            return workerNames.stream().distinct().count();
        }
    }
}
