package diranieh.concurrentHashing;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent hash set using lock striping
 *
 * Instead of using a single lock to synchronize the entire set {@link ConcurrentCoarseHashSet},
 * we split the set into independently synchronized pieces using a technique called lock striping.
 * For example, the implementation of ConcurrentHashMap uses an array of 16 locks, each of which
 * guards 1/16 of the hash buckets; bucket N is guarded by lock N mod 16. This should reduce the
 * demand for any given lock by a factor of 16 assuming the hash function provides reasonable
 * spreading and keys are accessed uniformly. It is this technique that allows ConcurrentHashMap
 * to support up to 16 concurrent writers.
 *
 * With lock striping, an operation can usually be performed by acquiring at most one lock, but
 * occasionally you need to lock the entire collection, as when ConcurrentHashMap needs to expand
 * the map and rehash the values into a larger set of buckets. This is done by acquiring all of
 * the locks in the stripe set.
 *
 * Synchronization policy: although the locks[] array is initially of the same capacity
 * as the table[] array (underlying hash table), table[] will grow when the set is resized,
 * but lock[] will not grow. This means that the ith lock protects table entry j where
 * j = i.hashCode % lock.length
 * Recall that that the modulus operator is cyclic meaning the hash code will repeat every
 * lock.length items.
 *
 * @param <E> the type of elements in this hash set
 */
public class ConcurrentStripedHashSet<E> extends BaseHashSet<E> {
    private final int bucketSizeThreshold;
    private final Lock[] locks;
    /**
     * @param capacity: size of the underlying hash table
     * @param bucketSizeThreshold average size bucket above which a resize is triggered
     */
    public ConcurrentStripedHashSet(int capacity, int bucketSizeThreshold) {
        super(capacity);

        // Initialize an array of locks that is the same size as the underlying
        // hash table (initialized in the base class)
        locks = new Lock[capacity];
        for (int i = 0; i < capacity; i++)
            locks[i] = new ReentrantLock();

        this.bucketSizeThreshold = bucketSizeThreshold;
    }

    @Override
    protected void acquire(E x) {
        locks[getLockIndex(x)].lock();
    }

    @Override
    protected void release(E x) {
        locks[getLockIndex(x)].unlock();
    }

    @Override
    protected void resize() {
        int oldCapacity = table.length;     // Helps implement a thread-safe check-then-act below

        // Acquire locks in an ascending order
        for (Lock lock: locks) {
            lock.lock();
        }
        try {
            // The base class employs a check-then-act idiom in the add method (if shouldResize
            // then resize). One option would have been be to do the check-then-act inside the
            // synchronized block of the add method. The code below shows another approach to
            // ensure that the check-then-act in the base class is thread-safe
            if (oldCapacity != table.length)
                return;     // already done by another thread. Nothing to do

            // Create and initialize the underlying hash table which is twice as big as the
            // old one
            int newCapacity = table.length * 2;
            List<E>[] newTable = createAndInitializeHashTable(newCapacity);

            // Copy existing data to the new table
            for (List<E> bucket: table) {
                // Iterate over each item in the current bucket, calculating a new
                // hash code and inserting in the appropriate bucket
                for (E item: bucket) {
                    int hashCode = calculateHashCode(item);
                    newTable[hashCode].add(item);
                }
            }

            // Update the underlying hash table
            table = newTable;
        } finally {
            // Release locks in an ascending order
            for (Lock lock: locks) {
                lock.unlock();
            }
        }
    }

    @Override
    protected boolean shouldResize() {
        double avgBucketSize = 1.0 * size.get() / table.length;
        return avgBucketSize > bucketSizeThreshold;
    }

    private int getLockIndex(E item) {
        return (item.hashCode() & CLEAR_MSB) % locks.length;
    }
}
