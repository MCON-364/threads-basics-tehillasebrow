package edu.touro.mcon364.concurrency.lesson1.exercises;

/**
 * A simple thread-safe counter for Lesson 1.
 * Uses synchronized methods to ensure correctness.
 */
public class ThreadSafeTaskCounter {

    private int count = 0;

    /** Increments the counter by one in a thread-safe manner. */
    public synchronized void increment() {
        count++;
    }

    /** Decrements the counter by one in a thread-safe manner. */
    public synchronized void decrement() {
        count--;
    }

    /** Returns the current counter value. */
    public synchronized int getCount() {
        return count;
    }

    /** Resets the counter back to zero. */
    public synchronized void reset() {
        count = 0;
    }
}
