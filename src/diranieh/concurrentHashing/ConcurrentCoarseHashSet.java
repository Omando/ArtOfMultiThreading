package diranieh.concurrentHashing;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A coarse-grained concurrent hash set
 * @param <E> the type of elements in this hash set
 */
public class ConcurrentCoarseHashSet<E> extends BaseHashSet<E> {
    private final Lock lock;
    private final int bucketSizeThreshold;

    /**
     * @param capacity: size of the underlying hash table
     * @param bucketSizeThreshold average size bucket above which a resize is triggered
     */
    public ConcurrentCoarseHashSet(int capacity, int bucketSizeThreshold) {
        super(capacity);

        lock = new ReentrantLock();
        this.bucketSizeThreshold = bucketSizeThreshold;
    }
    @Override
    protected void acquire(E dummy) {
        lock.lock();
    }

    @Override
    protected void release(E dummy) {
        lock.unlock();
    }

    @Override
    protected void resize() {
        int oldCapacity = table.length;     // Helps implement a thread-safe check-then-act below
        acquire(null);
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
            release(null);
        }
    }

    @Override
    protected boolean shouldResize() {
        double avgBucketSize = 1.0 * size.get() / table.length;
        return avgBucketSize > bucketSizeThreshold;
    }
}
