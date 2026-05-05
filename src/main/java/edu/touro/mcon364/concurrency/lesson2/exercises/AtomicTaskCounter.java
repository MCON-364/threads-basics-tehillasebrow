package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exercise 1 — Refactor a synchronized counter to AtomicInteger.
 *
 * This version replaces method-level synchronization with an AtomicInteger,
 * which provides thread-safe, lock-free operations for simple numeric updates.
 */
public class AtomicTaskCounter {

    /**
     * AtomicInteger holds the counter value in a thread-safe way without
     * requiring the synchronized keyword.
     */
    private final AtomicInteger safeCount = new AtomicInteger(0);

    /**
     * Atomically increments the counter by one.
     */
    public void increment() {
        safeCount.incrementAndGet();
    }

    /**
     * Atomically decrements the counter by one.
     */
    public void decrement() {
        safeCount.decrementAndGet();
    }

    /**
     * Returns the current counter value.
     */
    public int getCount() {
        return safeCount.get();
    }

    /**
     * Resets the counter back to zero.
     */
    public void reset() {
        safeCount.set(0);
    }
}
