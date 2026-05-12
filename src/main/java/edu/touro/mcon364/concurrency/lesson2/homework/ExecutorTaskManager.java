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
     * If I needed to ensure that a full batch of tasks finished before starting
     * the next batch, I would use a CountDownLatch. Each task would decrement
     * the latch when finished, and the coordinating thread would simply wait
     * until the latch reaches zero before continuing.
     * ──────────────────────────────────────────────────────────────────────*/

    private static final int POOL_SIZE = 4;

    /** Fixed-size thread pool limits concurrency and reuses threads */
    private final ExecutorService pool =
            Executors.newFixedThreadPool(POOL_SIZE);

    /** Atomic counter guarantees unique IDs without explicit locking */
    private final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * Tasks that have finished executing.
     * This list is written by multiple worker threads.
     */
    private final List<Task> completedTasks = new ArrayList<>();

    /** Lock protecting concurrent access to completedTasks */
    private final ReentrantLock completedTasksLock = new ReentrantLock();

    // ── ID generation ────────────────────────────────────────────────────────

    /**
     * Returns a unique, auto-incremented task ID.
     * IDs start at 1 and increase monotonically.
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

        Callable<Task> callable = () -> {
            try {
                Thread.sleep(10); // simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }

            recordCompleted(task);
            return task;
        };

        return pool.submit(callable);
    }

    // ── recording completion ─────────────────────────────────────────────────

    /**
     * Records a finished task.
     *
     * This method is called concurrently by worker threads. A lock is necessary
     * because ArrayList is not thread-safe, and simultaneous writes could corrupt
     * its internal state or cause lost updates.
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
     * Shuts down the pool and waits for tasks to complete.
     */
    public void shutdown() throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    // ── observability ────────────────────────────────────────────────────────

    /**
     * Returns a snapshot of completed tasks.
     */
    public List<Task> getCompletedTasks() {
        completedTasksLock.lock();
        try {
            return new ArrayList<>(completedTasks);
        } finally {
            completedTasksLock.unlock();
        }
    }

    /**
     * Returns the most recently issued task ID.
     */
    public int getLastIssuedId() {
        return idCounter.get();
    }
}
