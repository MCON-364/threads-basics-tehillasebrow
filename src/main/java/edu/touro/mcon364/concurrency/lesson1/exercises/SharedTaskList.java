package edu.touro.mcon364.concurrency.lesson1.exercises;

import edu.touro.mcon364.concurrency.common.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exercise 4: Fixing a shared ArrayList race condition
 *
 * Multiple threads add Tasks to the shared list concurrently.
 * The plain ArrayList below is NOT thread-safe: concurrent structural
 * modifications can corrupt its internal state and lose elements.
 *
 * Solution: Replace the bare ArrayList with a thread-safe alternative so that
 * all tasks added by all threads are reliably present when every thread
 * has finished.
 *
 * Implementation: Collections.synchronizedList(new ArrayList<>())
 *
 * Do NOT change the add() or size() method signatures.
 */
public class SharedTaskList {

    /**
     * Thread-safe list that wraps a standard ArrayList with synchronization.
     * 
     * WHY THIS IS NEEDED:
     * - A plain ArrayList is NOT thread-safe for concurrent modifications
     * - When multiple threads call add() simultaneously, they can corrupt the internal
     *   array structure, causing elements to be lost or overwritten
     * - This is a classic "race condition" where the outcome depends on the timing of thread execution
     * 
     * HOW Collections.synchronizedList() WORKS:
     * - It wraps the ArrayList with built-in synchronization
     * - Every operation (add, remove, get, size, etc.) is protected by a monitor lock
     * - Only one thread can modify the list at a time, preventing corruption
     * - Other threads must wait their turn, but correctness is guaranteed
     * 
     * THREAD SAFETY GUARANTEE:
     * - All tasks added by all threads will be present in the list after all threads complete
     * - No elements will be lost or duplicated due to concurrent access
     * - The size() method will return accurate counts even during concurrent modifications
     */
    private final List<Task> tasks = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a task to the shared list in a thread-safe manner.
     * 
     * WHAT HAPPENS INTERNALLY:
     * - The synchronization wrapper ensures that the add operation is atomic
     * - While one thread is adding, all other threads attempting add() are blocked
     * - This prevents interleaved modifications that would corrupt the internal array
     * - Once this thread completes, the lock is released for other threads
     * 
     * @param task the Task to add to the list (must not be null)
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the current number of tasks in the list.
     * 
     * THREAD SAFETY NOTE:
     * - The size() call is also synchronized, so it returns an accurate count
     * - However, this count is a "snapshot" at the moment size() is called
     * - If other threads are concurrently adding tasks, the size may change immediately after
     * - This is normal and expected in concurrent programs
     * 
     * @return the number of tasks currently in the list
     */
    public int size() {
        return tasks.size();
    }
}
