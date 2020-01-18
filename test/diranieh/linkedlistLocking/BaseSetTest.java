package diranieh.linkedlistLocking;

/**
 * Base interface for testing concurrent sets
 * This interface is extended by both ConcurrentSetTests and SequentialSetTests interfaces.
 * Each concurrent set implementation has a test class that implements both ConcurrentSetTests
 * and SequentialSetTests interfaces. This means that the same sequential and concurrent tests
 * are used for each concurrent set implementation
 */
public interface BaseSetTest {
    Set<String> createSet();
    Set<String> createAndPopulateSet(Iterable<String> items);
}
