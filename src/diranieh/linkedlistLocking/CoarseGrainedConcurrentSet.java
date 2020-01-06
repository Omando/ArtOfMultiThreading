package diranieh.linkedlistLocking;

/**
 *  A concurrent linked list implementation of a set with coarse-grained synchronization
 *  SimpleLinkedListSet is used as the underlying Set implementation
 *
 *  If contention is very low, this class can be used when a  concurrent set
 *  is required. If there is contention, this class will neither scale nor
 *  perform. This is because threads will be delayed waiting for one another
 *  due to serialization and context switches.
 */
public class CoarseGrainedConcurrentSet<T> implements Set<T> {
    private final SimpleLinkedListSet<T> implementation = new SimpleLinkedListSet<>();

    @Override
    public synchronized boolean add(T item) {
        return implementation.add(item);
    }

    @Override
    public synchronized boolean remove(T item) {
        return implementation.remove(item);
    }

    @Override
    public synchronized boolean contains(T item) {
        return implementation.contains(item);
    }

    public synchronized boolean isEmpty() {
        return implementation.isEmpty();
    }
}
