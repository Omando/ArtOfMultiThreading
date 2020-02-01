package diranieh.concurrentQueues;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BoundedBlockingConcurrentQueueTest implements SequentialQueueTests, ConcurrentQueueTests {
    @Override
    public Queue<Integer> createQueue(int capacity) {
        return new BoundedBlockingConcurrentQueue<Integer>(capacity);
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacity, Iterable<Integer> items) throws InterruptedException {
        BoundedBlockingConcurrentQueue<Integer> queue = new BoundedBlockingConcurrentQueue<>(capacity);
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }

    @Test
    void should_block_enqueuing_when_full() throws InterruptedException {
        // Arrange
        final int capacity = 10;
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchFinish = new CountDownLatch(1);
        Queue<Integer> queue = createQueue(capacity);

        Thread thread = new Thread(() -> {
            // Populate queue to capacity
            for (int i = 0; i < capacity; ++i) {
                try {
                    queue.enqueue(i);
                } catch (InterruptedException ignored) {}
            }

            // Let test main thread know we're ready for interrupt
            latchStart.countDown();

            // Act - should block until interrupted
            try {
                queue.enqueue(1000);
                fail("Enqueue should have blocked");
            } catch (InterruptedException dummy) {
                Thread.currentThread().interrupt();     // restore interrupt status
                System.out.println("Interrupted successfully");
            } catch(AssertionFailedError dummy) {
                /* Nothing to do. Let thread die */
            }
             finally {
                // Let test main thread know we're done
                latchFinish.countDown();
            }
        });
        thread.start();

        // Wait for thread to populate queue and be ready to be interrupted, then proceed
        latchStart.await();
        Thread.sleep(100);      // Allow time for queue.enqueue to be called

        // Test would have failed, if queue.enqueue(1000) did not block
        thread.interrupt();

        // Wait for thread to complete then test the interrupt status flag
        latchFinish.await();
        boolean isInterrdupted = thread.isInterrupted();
        assertTrue(isInterrdupted);
    }

    @Test
    void should_block_dequeuing_when_empty() throws InterruptedException {
        // Arrange
        final int capacity = 10;
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchFinish = new CountDownLatch(1);
        Queue<Integer> queue = createQueue(capacity);

        Thread thread = new Thread(() -> {
            // Let test main thread know we're ready for interrupt
            latchStart.countDown();

            try {
                // Act - should block until interrupted
                queue.dequeue();
                fail("dequeue should have blocked");
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();     // restore interrupt status
                System.out.println("Interrupted successfully");
            } catch(AssertionFailedError dummy) {
                /* Nothing to do. Let thread die */
            }
            finally {
                // Let test main thread know we're done
                latchFinish.countDown();
            }
        });
        thread.start();

        // Wait for thread to be ready to be interrupted
        latchStart.await();
        Thread.sleep(100);      // Allow time for queue.dequeue to be called

        // Test would have failed if queue.dequeue did not block
        thread.interrupt();
        latchFinish.await();
        boolean isInterrdupted = thread.isInterrupted();
        assertTrue(isInterrdupted);
    }

    // This test is not valid for UnboundedConcurrentQueue; this class throws an
    // IllegalStateException if a thread attempts to dequeue from an empty queue.
    // All other queue implementations block until the queue is not empty.
    @RepeatedTest(100)
    void should_enqueue_with_parallel_enqueuers_and_dequeue_with_parallel_dequeuers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Queue<Integer> queue = createQueue(TEST_SIZE);
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