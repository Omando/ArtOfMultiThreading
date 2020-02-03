package diranieh.concurrentQueues;


/**
 * Base interface for testing concurrent queues
 * This interface is extended by both ConcurrentQueueTests and SequentialQueueTests interfaces.
 * Each concurrent queue implementation has a test class that implements both ConcurrentQueueTests
 * and SequentialQueueTests interfaces. This means that the same sequential and concurrent tests
 * are used for each concurrent queue implementation
 */
public interface BaseQueueTest<E> {
    final static int THREAD_COUNT = 32;
    final static int TEST_SIZE = 1024;
    final static int ITEMS_PER_THREAD = TEST_SIZE / THREAD_COUNT;       // 1024/32 = 32

    Queue<E> createQueue(int capacity);
    Queue<E> createAndPopulateQueue(int capacity, Iterable<E> items) throws InterruptedException;
}
