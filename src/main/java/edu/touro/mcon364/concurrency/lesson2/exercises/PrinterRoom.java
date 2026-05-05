package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercise 3 — Limiting concurrent access with Semaphore.
 *
 * Models a printer room with a fixed number of printers. A semaphore ensures
 * that no more than printerCount threads may print at the same time.
 */
public class PrinterRoom {

    private final int printerCount;

    /** Semaphore controlling access to the printers */
    private final Semaphore semaphore;

    // Counters visible to tests
    private final AtomicInteger activeCount   = new AtomicInteger(0);
    private final AtomicInteger maxObserved   = new AtomicInteger(0);
    private final AtomicInteger completedJobs = new AtomicInteger(0);

    public PrinterRoom(int printerCount) {
        this.printerCount = printerCount;

        // Exactly printerCount permits: at most that many concurrent print jobs
        this.semaphore = new Semaphore(printerCount);
    }

    /**
     * Simulate printing a document.
     * Blocks if all printers are currently in use.
     */
    public void print(String document) throws InterruptedException {

        // Acquire a permit before entering the printer room
        semaphore.acquire();

        try {
            // One more job becomes active
            int current = activeCount.incrementAndGet();

            // Update the maximum concurrency seen so far
            maxObserved.updateAndGet(prev -> Math.max(prev, current));

            // Simulate printing time
            Thread.sleep(50);

            // Record completed job
            completedJobs.incrementAndGet();

        } finally {
            // Always release the permit so another thread may print
            activeCount.decrementAndGet();
            semaphore.release();
        }
    }

    /** Returns the number of currently active print jobs. */
    public int getActiveCount() {
        return activeCount.get();
    }

    /** Returns the peak number of simultaneous print jobs observed. */
    public int getMaxObservedConcurrency() {
        return maxObserved.get();
    }

    /** Returns the total number of jobs that have completed. */
    public int getCompletedJobs() {
        return completedJobs.get();
    }

    public int getPrinterCount() {
        return printerCount;
    }
}
