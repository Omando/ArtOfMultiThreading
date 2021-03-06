package diranieh.concurrentQueues;

import org.junit.jupiter.api.RepeatedTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface ConcurrentQueueTests extends BaseQueueTest<Integer> {
    // Multiple threads are used to concurrently enqueue items. Sequential code then
    // checks that all items were inserted correctly
    @RepeatedTest(100)
    default void should_enqueue_with_parallel_enqueuers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Queue<Integer> queue = createQueue(TEST_SIZE);
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] threads = new Thread[THREAD_COUNT];

        // Create THREAD_COUNT threads with each thread populating the queue
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int startValue = i * ITEMS_PER_THREAD;
            threads[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads enqueue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        queue.enqueue(startValue + j);
                    }
                } catch (InterruptedException exception) {
                    System.out.println("Error enqueueing: " + exception.getMessage());
                    Thread.currentThread().interrupt();     // restore interrupt status
                }
            });
            threads[i].start();
        }

        // Start enqueueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT; i ++) {
            threads[i].join();
        }

        // Now check results by dequeueing all items sequentially and flag any duplicates
        for (int i = 0; i < TEST_SIZE; i++) {
            int j = queue.dequeue();
            if (poppedItems[j]) {
                fail("duplicate pop: " + j);
            } else {
                poppedItems[j] = true;
            }
        }
    }

    // Multiple threads are used to concurrently dequeue items. Sequential code is
    // used to populate the queue
    @RepeatedTest(100)
    default void should_dequeue_with_parallel_dequeuers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        List<Integer> numbers = List.of(IntStream.range(0, TEST_SIZE).boxed().toArray(Integer[]::new));
        final Queue<Integer> queue = createAndPopulateQueue(TEST_SIZE, numbers);
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] threads = new Thread[THREAD_COUNT];

        // Create THREAD_COUNT threads with each thread dequeueing the queue
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads dequeue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        int item = queue.dequeue();
                        if (poppedItems[item]) {
                            fail("duplicate pop: " + j);
                        } else {
                            poppedItems[item] = true;
                        }
                    }
                } catch (InterruptedException exception) {
                    System.out.println("Error dequeueing: " + exception.getMessage());
                    Thread.currentThread().interrupt();     // restore interrupt status
                }
            });
            threads[i].start();
        }

        // Start dequeueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT; i ++) {
            threads[i].join();
        }

        // Now check results by dequeueing all items sequentially and flag any duplicates
        for (int i = 0; i < TEST_SIZE; i++) {
            assertTrue(poppedItems[i]);
        }
    }
}
