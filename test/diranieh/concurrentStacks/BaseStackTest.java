package diranieh.concurrentStacks;

/**
 * Base interface for testing concurrent stacks
 * This interface is extended by both ConcurrentStackTests and SequentialStackTests interfaces.
 * Each concurrent stack implementation has a test class that implements both ConcurrentStackTests
 * and SequentialStackTests interfaces. This means that the same sequential and concurrent tests
 * are used for each concurrent stack implementation
 */
public interface BaseStackTest<E> {
    final static int THREAD_COUNT = 32;
    final static int TEST_SIZE = 1024;
    final static int ITEMS_PER_THREAD = TEST_SIZE / THREAD_COUNT;       // 1024/32 = 32

    Stack<E> createStack();
}
