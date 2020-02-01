package diranieh.concurrentQueues;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
