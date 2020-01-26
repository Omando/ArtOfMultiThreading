package diranieh.concurrentQueues;

import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.fail;

public interface ConcurrentQueueTests extends BaseQueueTest<Integer> {
    final static int THREAD_COUNT = 32;
    final static int TEST_SIZE = 1024;
    final static int ITEMS_PER_THREAD = TEST_SIZE / THREAD_COUNT;       // 1024/32 = 32

    // Multiple threads are used to concurrently enqueue items. Sequential code then
    // checks that all items were inserted correctly
    @RepeatedTest(100)
    default void should_enqueue_with_parallel_enqueuers() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Queue<Integer> queue = createQueue(TEST_SIZE);
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] thread = new Thread[THREAD_COUNT];

        // Create THREAD_COUNT threads with each thread  populating the queue
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int startValue = i * ITEMS_PER_THREAD;
            thread[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads enqueue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        queue.enqueue(startValue + j);
                    }
                } catch (InterruptedException exception) {
                    System.out.println("Error enqueueing: " + exception.getMessage());
                }
            });
            thread[i].start();
        }

        // Start enqueueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT; i ++) {
            thread[i].join();
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
}
