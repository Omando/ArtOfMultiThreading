package diranieh.concurrentQueues;


/**
 * Base interface for testing concurrent queues
 * This interface is extended by both ConcurrentQueueTests and SequentialQueueTests interfaces.
 * Each concurrent queue implementation has a test class that implements both ConcurrentQueueTests
 * and SequentialQueueTests interfaces. This means that the same sequential and concurrent tests
 * are used for each concurrent queue implementation
 */
public interface BaseQueueTest<E> {
    Queue<E> createQueue(int capacity);
    Queue<E> createAndPopulateQueue(int capacity, Iterable<E> items) throws InterruptedException;
}
