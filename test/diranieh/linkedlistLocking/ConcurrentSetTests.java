package diranieh.linkedlistLocking;

import diranieh.utilities.Counter;
import diranieh.utilities.Set;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public interface ConcurrentSetTests extends BaseSetTest  {

    public static final ExecutorService executor = Executors.newFixedThreadPool(100);

    @RepeatedTest(100)
    default void when_multiple_threads_add_same_value_one_thread_succeeds() throws InterruptedException {
        // Arrange
        int threadCount = 100;
        Counter counter = new Counter();
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchEnd = new CountDownLatch(threadCount);
        Set<String> concurrentSet = createAndPopulateSet(List.of("A", "B", "C", "D"));

        // Act: multiple threads attempting to add the same value
        final String newValue = "E";
        for (int i = 0; i < threadCount; ++i) {
            executor.execute(() -> {
                // Wait for signal from master thread to start running
                try {
                    latchStart.await();
                    boolean added = concurrentSet.add(newValue);
                    if (added) counter.increment();
                    latchEnd.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Thread interrupted");
                }
            });
        }

        latchStart.countDown();     // start all waiting threads
        latchEnd.await();           // wait for all threads to finish

        // Assert value is added by one thread only
        assertTrue(concurrentSet.contains(newValue));
        assertEquals(1, counter.get());
    }

    // This test may occasionally fail for NonBlockingConcurrentSet. Please see note at the top
    // of that class
    @RepeatedTest(100)
    default void when_multiple_threads_remove_same_value_one_thread_succeeds() throws InterruptedException {
        // Arrange
        int threadCount = 100;
        Counter counter = new Counter();
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchEnd = new CountDownLatch(threadCount);
        Set<String> concurrentSet = createAndPopulateSet(List.of("A", "B", "C", "D"));

        // Act: multiple threads attempting to remove the same value
        final String valueToRemove = "A";
        for (int i = 0; i < threadCount; ++i) {
            executor.execute(() -> {
                // Wait for signal from master thread to start running
                try {
                    latchStart.await();
                    boolean removed = concurrentSet.remove(valueToRemove);
                    if (removed) counter.increment();
                    latchEnd.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Thread interrupted");
                }
            });
        }

        latchStart.countDown();     // start all waiting threads
        latchEnd.await();           // wait for all threads to finish

        // Assert value is is removed by one thread only
        assertFalse(concurrentSet.contains(valueToRemove));
        assertEquals(1, counter.get());
    }

    @RepeatedTest(100)
    default void when_multiple_threads_add_different_values_all_threads_succeed() throws InterruptedException {
        // Arrange
        int threadCount = 100;
        Counter counter = new Counter();
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchEnd = new CountDownLatch(threadCount);
        Set<String> concurrentSet = createSet();

        // Act: multiple threads each attempting to add a different value
        for (int i = 0; i < threadCount; ++i) {
            final int index = i;
            executor.execute(() -> {
                // Wait for signal from master thread to start running
                try {
                    latchStart.await();
                    boolean added = concurrentSet.add(Integer.toString(index));
                    if (added) counter.increment();
                    latchEnd.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Thread interrupted");
                }
            });
        }

        latchStart.countDown();     // start all waiting threads
        latchEnd.await();           // wait for all threads to finish

        // Assert that each thread added a value
        assertEquals(threadCount, counter.get());
    }

    @RepeatedTest(100)
    default void when_multiple_threads_remove_different_values_all_threads_succeed() throws InterruptedException {
        // Arrange
        int threadCount = 100;
        Counter counter = new Counter();
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchEnd = new CountDownLatch(threadCount);

        List<String> items = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; ++i)
            items.add(Integer.toString(i));
        Set<String> concurrentSet = createAndPopulateSet(items);

        // Act: multiple threads each attempting to remove a different value
        for (int i = 0; i < threadCount; ++i) {
            final int index = i;
            executor.execute(() -> {
                // Wait for signal from master thread to start running
                try {
                    latchStart.await();
                    boolean added = concurrentSet.remove(Integer.toString(index));
                    if (added) counter.increment();
                    latchEnd.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Thread interrupted");
                }
            });
        }

        latchStart.countDown();     // start all waiting threads
        latchEnd.await();           // wait for all threads to finish

        // Assert that each thread removed a value
        assertEquals(threadCount, counter.get());
    }
}
