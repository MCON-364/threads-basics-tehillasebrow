import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ThreadSafeTaskCounter class is a thread-safe implementation for counting tasks.
 * This class uses an AtomicInteger to ensure that increment operations are safe
 * for concurrent access, which prevents race conditions and ensures data integrity.
 */
public class ThreadSafeTaskCounter {
    // An AtomicInteger to hold the count of tasks safely across multiple threads.
    private final AtomicInteger taskCount = new AtomicInteger(0);

    /**
     * Increments the task count by one in a thread-safe manner.
     * @return the updated count of tasks after incrementing.
     */
    public int increment() {
        // Increment the task count atomically and return the new value.
        return taskCount.incrementAndGet();
    }

    /**
     * Returns the current task count.
     * @return the current count of tasks.
     */
    public int getCount() {
        // Return the current value of the task count.
        return taskCount.get();
    }

    /**
     * Resets the count of tasks to zero. This operation should be used with caution
     * in multi-threaded contexts, as simultaneous resets while counting can
     * lead to lost updates. Users should synchronize this call if needed.
     */
    public void reset() {
        taskCount.set(0); // Reset the task count to zero.
    }
}

/**
 * The main method demonstrates the usage of the ThreadSafeTaskCounter
 * in a multi-threaded environment.
 */
class Main {
    public static void main(String[] args) {
        ThreadSafeTaskCounter counter = new ThreadSafeTaskCounter();
        // Creating multiple threads that increment the task counter.
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            }).start();
        }
        // Wait for a moment to ensure that all threads finish their execution.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Print the final count of tasks.
        System.out.println("Total tasks counted: " + counter.getCount());
    }
}
