package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Exercise 2 — Startup coordination with CountDownLatch.
 *
 * Demonstrates how a coordinating thread can wait for multiple workers
 * to reach a certain point (startup) without waiting for them to finish.
 */
public class WorkerStartupLatch {

    // Written by launchAndWait(), read by other threads
    private volatile boolean allStarted = false;

    // Records worker thread names as they check in
    private final List<String> startedNames = new ArrayList<>();

    /**
     * Launch {@code workerCount} threads, wait for all to check in,
     * then mark startup as complete.
     */
    public void launchAndWait(int workerCount) throws InterruptedException {

        // Latch starts with one count per worker
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            int id = i;

            Thread worker = new Thread(() -> {
                try {
                    // Simulate startup work
                    Thread.sleep(id * 200L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Record this worker's name safely
                synchronized (startedNames) {
                    startedNames.add(Thread.currentThread().getName());
                }

                // Signal that this worker has finished startup
                latch.countDown();

            }, "worker-" + id);

            worker.start();
        }

        // Wait until ALL workers have called countDown()
        latch.await();

        // Mark startup phase as complete
        allStarted = true;
    }

    /** Returns true once all workers have signaled readiness. */
    public boolean isAllStarted() {
        return allStarted;
    }

    /** Returns the names of threads that checked in (order may vary). */
    public List<String> getStartedNames() {
        synchronized (startedNames) {
            return List.copyOf(startedNames);
        }
    }
}
