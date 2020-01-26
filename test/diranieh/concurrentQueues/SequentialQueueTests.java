package diranieh.concurrentQueues;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public interface SequentialQueueTests extends BaseQueueTest<Integer> {
    final static int SAMPLE_SIZE = 512;

    @Test
    default void new_queue_should_be_empty() {
        // Arrange & Act
        Queue<Integer> queue = createQueue(10);

        // Assert
        assertTrue(queue.isEmpty());
    }

    @Test
    default void should_enqueue_and_dequeue() throws InterruptedException {
        // Arrange
        Queue<Integer> queue = createQueue(SAMPLE_SIZE);

        // Act
        for(int i = 0; i < SAMPLE_SIZE; ++i) {
            queue.enqueue(i);
        }
        for(int i = 0; i < SAMPLE_SIZE; ++i) {
            int item = queue.dequeue();
            assertEquals(i, item);
        }
    }

    @Test
    default void should_block_enqueuing_when_full() throws InterruptedException {
        // Arrange
        final int capacity = 10;
        CountDownLatch latch = new CountDownLatch(1);
        Queue<Integer> queue = createQueue(capacity);

        Thread thread = new Thread(() -> {
            // Populate queue to capacity
            for (int i = 0; i < capacity; ++i) {
                try {
                    queue.enqueue(i);
                } catch (InterruptedException ignored) {}
            }

            // Let test main thread know we're ready for interrupt
            latch.countDown();

            // Act - should block until interrupted
            try {
                queue.enqueue(1000);
                fail("Enqueue should have blocked");
            } catch (InterruptedException ignored) {
                System.out.println("Interrupted successfully");
            }
        });
        thread.start();

        // Wait for thread to populate queue and be ready to be interrupted
        latch.await();
        Thread.sleep(100);      // Allow time for queue.enqueue to be called

        // Test would have failed, if queue.enqueue(1000) did not block
        thread.interrupt();
        thread.join();
    }

    @Test
    default void should_block_dequeuing_when_empty() throws InterruptedException {
        // Arrange
        final int capacity = 10;
        CountDownLatch latch = new CountDownLatch(1);
        Queue<Integer> queue = createQueue(capacity);

        Thread thread = new Thread(() -> {
            // Let test main thread know we're ready for interrupt
            latch.countDown();

            try {
                // Act - should block until interrupted
                queue.dequeue();
                fail("dequeue should have blocked");
            } catch (InterruptedException ignored) {
                System.out.println("Interrupted successfully");
            }
        });
        thread.start();

        // Wait for thread to be ready to be interrupted
        latch.await();
        Thread.sleep(100);      // Allow time for queue.dequeue to be called

        // Test would have failed if queue.dequeue did not block
        thread.interrupt();
        thread.join();
    }
}
