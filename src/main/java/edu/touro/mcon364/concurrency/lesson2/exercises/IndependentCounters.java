package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Exercise 6 — Replace a coarse object lock with targeted ReentrantLocks.
 *
 * This version removes method-level synchronization and replaces it
 * with two independent locks to reduce unnecessary contention.
 */
public class IndependentCounters {

    private int readCount  = 0;
    private int writeCount = 0;

    /**
     * Separate locks guard independent pieces of state.
     * Using two locks allows reads and writes to proceed concurrently
     * when they do not touch the same data.
     */
    private final Lock readLock  = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

    /**
     * Record a read operation.
     * Uses a dedicated lock to protect readCount.
     */
    public void read() {
        readLock.lock();
        try {
            readCount++;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Record a write operation.
     * Uses a dedicated lock to protect writeCount.
     */
    public void write() {
        writeLock.lock();
        try {
            writeCount++;
        } finally {
            writeLock.unlock();
        }
    }

    public int getReadCount() {
        return readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }
}
