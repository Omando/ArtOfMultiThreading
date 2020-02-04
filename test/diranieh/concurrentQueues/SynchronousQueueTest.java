package diranieh.concurrentQueues;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SynchronousQueueTest {
    final static int THREAD_COUNT = 32;
    final static int TEST_SIZE = 1024;
    final static int ITEMS_PER_THREAD = TEST_SIZE / THREAD_COUNT;       // 1024/32 = 32

    private Queue<Integer> createQueue() {
        return new SynchronousQueue<>();
    }

    @Test
    void new_queue_should_be_empty() {
        // Arrange & Act
        Queue<Integer> queue = createQueue();

        // Assert
        assertTrue(queue.isEmpty());
    }

    @RepeatedTest(100)
    void should_enqueue_with_parallel_enqueuers_and_dequeue_with_parallel_dequeuers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Queue<Integer> queue = createQueue();
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] threads = new Thread[THREAD_COUNT * 2]; // THREAD_COUNT threads for enqueueing

        // Create THREAD_COUNT threads with each thread populating the queue
        // with ITEMS_PER_THREAD items
        // Create THREAD_COUNT threads with each thread dequeueing the queue
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {

            // Enqueueing
            final int startValue = i * ITEMS_PER_THREAD;
            threads[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads enqueue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        queue.enqueue(startValue + j);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Error enqueueing: " + exception.getMessage());
                }
            });

            // Dequeueing
            threads[i + THREAD_COUNT] = new Thread( () -> {
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
                    Thread.currentThread().interrupt();     // restore interrupt status
                    System.out.println("Error dequeueing: " + exception.getMessage());
                }
            });

            threads[i].start();
            threads[i+THREAD_COUNT].start();
        }

        // Start enqueueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT * 2; i ++) {
            threads[i].join();
        }

        // Now check results by dequeueing all items sequentially and flag any duplicates
        for (int i = 0; i < TEST_SIZE; i++) {
            assertTrue(poppedItems[i]);
        }
    }
}