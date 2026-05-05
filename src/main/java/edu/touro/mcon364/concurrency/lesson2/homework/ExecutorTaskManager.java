package edu.touro.mcon364.concurrency.lesson2.homework;

import edu.touro.mcon364.concurrency.common.model.Priority;
import edu.touro.mcon364.concurrency.common.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Homework — Executor-backed task manager with atomic IDs.
 */
public class ExecutorTaskManager {

    /* ── SYNCHRONIZER CHOICE ────────────────────────────────────────────────
     * If I needed to wait for an entire batch of tasks to finish before starting
     * the next batch, I would use a CountDownLatch. Each task would count down
     * when finished, and the coordinating thread could await completion of
     * the whole batch before continuing.
     * ──────────────────────────────────────────────────────────────────────*/

    private static final int POOL_SIZE = 4;

    /** Fixed-size thread pool ensures bounded concurrency */
    private final ExecutorService pool =
            Executors.newFixedThreadPool(POOL_SIZE);

    /** Atomic counter guarantees unique IDs without synchronization */
    private final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * List of tasks that have finished execution.
     * Written by multiple worker threads, so it must be protected.
     */
    private final List<Task> completedTasks = new ArrayList<>();

    /** Explicit lock protecting completedTasks */
    private final ReentrantLock completedTasksLock = new ReentrantLock();

    // ── ID generation ────────────────────────────────────────────────────────

    /**
     * Returns a unique, auto-incremented task ID.
     * IDs start at 1 and increase atomically.
     */
    public int nextId() {
        return idCounter.incrementAndGet();
    }

    // ── task submission ──────────────────────────────────────────────────────

    /**
     * Creates a Task and submits it to the thread pool for execution.
     */
    public Future<Task> submit(String description, Priority priority) {

        int id = nextId();
        Task task = new Task(id, description, priority);

        Callable<Task> work = () -> {
            try {
                Thread.sleep(10); // simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            recordCompleted(task);
            return task;
        };

        return pool.submit(work);
    }

    // ── recording completion ─────────────────────────────────────────────────

    /**
     * Records a finished task.
     *
     * Multiple worker threads may call this method concurrently.
     * Without a lock, simultaneous writes to ArrayList could corrupt
     * its internal structure and cause data loss or runtime exceptions.
     */
    private void recordCompleted(Task task) {
        completedTasksLock.lock();
        try {
            completedTasks.add(task);
        } finally {
            completedTasksLock.unlock();
        }
    }

    // ── collecting results ───────────────────────────────────────────────────

    /**
     * Waits for every future to complete and returns the completed Tasks
     * in submission order.
     */
    public List<Task> awaitAll(List<Future<Task>> futures) {
        List<Task> results = new ArrayList<>();

        for (Future<Task> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for task", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Task execution failed", e.getCause());
            }
        }

        return results;
    }

    // ── lifecycle ────────────────────────────────────────────────────────────

    /**
     * Shuts down the pool and waits up to 30 seconds for tasks to finish.
     */
    public void shutdown() throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    // ── observability ────────────────────────────────────────────────────────

    /**
     * Returns a defensive copy of completed tasks.
     */
    public List<Task> getCompletedTasks() {
        completedTasksLock.lock();
        try {
            return new ArrayList<>(completedTasks);
        } finally {
            completedTasksLock.unlock();
        }
    }

    /** Returns the most recently issued task ID. */
    public int getLastIssuedId() {
        return idCounter.get();
    }
}
