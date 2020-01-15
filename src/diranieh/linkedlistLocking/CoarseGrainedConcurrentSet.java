package diranieh.linkedlistLocking;

/**
 *  A concurrent linked list implementation of a set with coarse-grained
 *  synchronization. SimplerLinkedListSet is used as the underlying Set
 *  implementation.
 *
 *  If contention is very low, this class can be used when a concurrent set
 *  is required. If there is contention, this class will neither scale nor
 *  perform. This is because threads will be delayed waiting for one another
 *  due to serialization and context switches.
 *
 *  @param <E> the type of elements in this list
 */
public class CoarseGrainedConcurrentSet<E> implements Set<E> {
    private final SimplerLinkedListSet<E> implementation = new SimplerLinkedListSet<>();

    @Override
    public synchronized boolean add(E item) {
        return implementation.add(item);
    }

    @Override
    public synchronized boolean remove(E item) {
        return implementation.remove(item);
    }

    @Override
    public synchronized boolean contains(E item) {
        return implementation.contains(item);
    }

    public synchronized boolean isEmpty() {
        return implementation.isEmpty();
    }
}
