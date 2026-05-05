package edu.touro.mcon364.concurrency.lesson1.exercises;

/**
 * Exercise 3: sleep(), isAlive(), and the daemon flag
 *
 * A PeriodicLogger should log a message on a background thread at a fixed
 * interval for a fixed number of ticks, then stop.
 *
 * Requirements:
 * - start(): create a background thread that sleeps for intervalMs milliseconds,
 *   then appends one line to the log, and repeats this exactly 'ticks' times total.
 *   The thread must be marked as a DAEMON thread before it is started.
 *   The thread must be given the name "periodic-logger".
 *   start() must return BEFORE the background thread has finished (non-blocking).
 *
 * - isRunning(): return true if the background thread exists and is still alive,
 *   false otherwise.
 *
 * - awaitCompletion(): block the calling thread until the background thread finishes.
 *
 * - getLog(): return the messages appended so far (one per tick).
 *
 * Hint: Thread.sleep(intervalMs) pauses the current thread.
 *       thread.isAlive() tells you whether a thread is still running.
 *       setDaemon(true) must be called BEFORE start().
 */
public class PeriodicLogger {

    private final int ticks;
    private final long intervalMs;
    // Shared mutable state that may be accessed by multiple threads
    private final java.util.List<String> log = new java.util.ArrayList<>();
    // Reference to the background worker thread
    private Thread worker;

    public PeriodicLogger(int ticks, long intervalMs) {
        if (ticks <= 0) throw new IllegalArgumentException("ticks must be positive");
        if (intervalMs < 0) throw new IllegalArgumentException("intervalMs must be non-negative");
        this.ticks = ticks;
        this.intervalMs = intervalMs;
    }

    /**
     * Create, configure, and start the background thread.
     * Must return before the thread finishes (i.e. do NOT join here).
     */
    public void start() {
        // Create a new Thread with a lambda expression (Runnable implementation)
        // This thread will execute the code in the lambda body on a separate thread of execution
        worker = new Thread(() -> {
            // Loop 'ticks' times (1-based for user-friendly output)
            for (int i = 1; i <= ticks; i++) {
                try {
                    // Thread.sleep() pauses the current thread (the worker thread) for the specified
                    // milliseconds. This prevents the loop from running in a busy-wait, saving CPU.
                    // InterruptedException is thrown if this thread is interrupted while sleeping.
                    Thread.sleep(intervalMs);
                    
                    // CRITICAL: Synchronize access to the shared 'log' list because multiple threads
                    // might access it (e.g., main thread calls getLog() while worker thread writes).
                    // Without synchronization, we risk corrupting the list or missing updates.
                    synchronized (log) {
                        log.add("tick " + i);
                    }
                } catch (InterruptedException e) {
                    // If the worker thread is interrupted, we politely restore the interrupt flag
                    // so higher-level code can detect the interruption, then exit the loop early.
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // When the loop completes (or is interrupted), the worker thread exits and terminates.
        });
        
        // Set the thread name to "periodic-logger" for debugging and logging purposes.
        // Thread names help identify which thread is doing what in logs and stack traces.
        worker.setName("periodic-logger");
        
        // Mark this thread as a DAEMON thread. A daemon thread will NOT prevent the JVM from
        // exiting when all non-daemon (user) threads have finished. This is useful for background
        // tasks that don't need to complete before the program terminates.
        // IMPORTANT: setDaemon() must be called BEFORE start()
        worker.setDaemon(true);
        
        // Start the background thread. This schedules it to run concurrently with the main thread.
        // start() returns immediately (non-blocking); the worker thread will begin executing in parallel.
        worker.start();
    }

    /**
     * Returns true while the background thread is still running.
     */
    public boolean isRunning() {
        // Thread.isAlive() returns true if the thread has been started and has not yet terminated.
        // We also check if worker != null to handle the case where start() was never called.
        return worker != null && worker.isAlive();
    }

    /**
     * Blocks until the background thread finishes.
     */
    public void awaitCompletion() throws InterruptedException {
        // Thread.join() causes the calling thread (usually the main thread) to pause and wait until
        // the worker thread has finished executing. This is a blocking operation that ensures we
        // synchronize the execution of two threads at this point.
        worker.join();
    }

    /** Returns the log messages collected so far. */
    public java.util.List<String> getLog() {
        // Return an unmodifiable (read-only) view of the log to prevent external code from
        // accidentally modifying the internal list, which could corrupt thread-safe operations.
        return java.util.Collections.unmodifiableList(log);
    }
}
