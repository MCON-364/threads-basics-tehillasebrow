package edu.touro.mcon364.concurrency.lesson2.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Exercise 5 — Returning values from concurrent tasks with Callable and Future.
 *
 * Splits a list of integers across multiple workers, sums each slice in parallel,
 * and combines the results.
 */
public class ParallelSumCalculator {

    /**
     * Computes the sum of {@code numbers} by splitting the work across
     * {@code workers} Callable tasks submitted to a thread pool.
     */
    public long parallelSum(List<Integer> numbers, int workers)
            throws InterruptedException, ExecutionException {

        // Create a fixed-size thread pool
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        // This will hold the results of each worker's computation
        List<Future<Long>> futures = new ArrayList<>();

        int size = numbers.size();
        int sliceSize = (size + workers - 1) / workers; // round up

        // Submit one task per slice
        for (int i = 0; i < workers; i++) {
            int start = i * sliceSize;
            int end = Math.min(start + sliceSize, size);

            // Guard against empty slices when workers > numbers
            if (start >= end) {
                break;
            }

            List<Integer> slice = numbers.subList(start, end);

            Callable<Long> task = () -> {
                long partialSum = 0;
                for (int n : slice) {
                    partialSum += n;
                }
                return partialSum;
            };

            futures.add(pool.submit(task));
        }

        // Collect results AFTER all tasks have been submitted
        long total = 0;
        for (Future<Long> future : futures) {
            total += future.get();
        }

        // Shut down the pool and release resources
        pool.shutdown();

        return total;
    }
}
