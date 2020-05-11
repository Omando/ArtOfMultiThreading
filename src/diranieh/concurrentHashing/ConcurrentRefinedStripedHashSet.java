package diranieh.concurrentHashing;

import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

/*  ConcurrentRefinedStripedHashSet refines the granularity of locking as the table
size grows sl that the number of locations in a stripe does not continuously grow.
 To add a higher level of synchronization, we introduce a globally shared owner field
 that combines two values: a boolean value and a reference to a thread. These two values
 are combined in an AtomicMarkableReference<Thread> to allow them to be modified atomically.

 On startup, the thread reference is null and the boolean value is false, meaning that the
 set is not in the middle of resizing. While a resizing is in progress, however, the Boolean
 value is true, and the associated reference indicates the thread that is in charge of resizing.
  We use the owner as a mutual exclusion flag between the resize() method and any of the add()
  methods, so that while resizing, there will be no successful updates, and while updating,
  there will be no successful resizes.
* */
public class ConcurrentRefinedStripedHashSet<E> extends BaseHashSet<E> {
    // An AtomicMarkableReference maintains an object reference along with
    // a mark bit, that can be updated atomically.
    private AtomicMarkableReference<Thread> owner;
    private final int bucketSizeThreshold;
    volatile ReentrantLock[] locks;

    public ConcurrentRefinedStripedHashSet(int initialCapacity,  int bucketSizeThreshold) {
        super(initialCapacity);

        locks = new ReentrantLock[initialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            locks[i] = new ReentrantLock();
        }

        this.bucketSizeThreshold = bucketSizeThreshold;

        // owner field: On startup, there is no owner (null) and we are not
        // resizing (false). These two values are combined in an instance of
        // AtomicMarkableReference so that both can be updated atomically.
        // While a resizing is in progress, the Boolean value is true, and the
        // associated reference indicates the thread that is in charge of resizing
        owner = new AtomicMarkableReference<>(null, false);
    }

    @Override
    protected void acquire(E item) {
        // Prepare variables for the AtomicMarkableReference field (owner)
        boolean[] mark = new boolean[1];    // {true};
        Thread currentThread = Thread.currentThread();
        Thread currentOwningThread;
        boolean isMarked;
        boolean ownerThreadIsNotCurrentThread;

        // Spin until the lock is acquired
        while (true) {

            // Spin until other threads are done resizing the set. In other words,
            // as long as mark[0] is true and the owning thread is not this current
            // thread, then continue on spinning. We stop spinning in 3 cases:
            // mark[0] is false
            // currentOwningThread = currentThread
            // mark[0] is false AND currentOwningThread = currentThread
            do {
                currentOwningThread = owner.get(mark);     //  On return, mark[0] will hold the value of the mark.
                isMarked = mark[0];
                ownerThreadIsNotCurrentThread = currentOwningThread != currentThread;

            } while (isMarked && ownerThreadIsNotCurrentThread);

            /* Bookmark1 */
            // Cache a copy of locks array
            ReentrantLock[] oldLocks = locks;

            // Acquire the lock for the given item
            int lockIndex = (item.hashCode() & CLEAR_MSB) % oldLocks.length;
            ReentrantLock lock =  oldLocks[lockIndex];
            lock.lock();
            /* Bookmark2 */

            // Check again while holding the lock to make sure no other thread
            // is resizing, and that no resizing took between bookmarks 1 & 2
            currentOwningThread = owner.get(mark);     //  On return, mark[0] will hold the value of the mark.
            if ( (!mark[0] || currentOwningThread == currentThread) && (locks == oldLocks))
                return;         // Lock acquired

            // the acquired lock is out-of-date because of an ongoing update. Release
            // the lock and loop again
            lock.unlock();
        }
    }

    @Override
    protected void release(E item) {
        int lockIndex = (item.hashCode() & CLEAR_MSB) % locks.length;
        locks[lockIndex].unlock();
    }

    @Override
    protected void resize() {
        int oldCapacity = table.length;
        int newCapacity = 2 * oldCapacity;
        boolean[] mark = {false};
        Thread currentThread = Thread.currentThread();

        if (owner.compareAndSet(null, currentThread, false, true)){
            try {
                // Return if someone resized first
                if (table.length != oldCapacity)
                    return;

                // ensure that no other thread is in the middle of an add(), remove(), or
                // contains() call; visit each lock and waits until it is unlocked.
                for (ReentrantLock lock: locks)
                    while (lock.isLocked()) { /* Spin */}

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

                locks = new ReentrantLock[newCapacity];
                for (int j = 0; j < locks.length; j++) {
                    locks[j] = new ReentrantLock();
                }

            } finally {
                owner.set(null, false);
            }
        }
    }

    @Override
    protected boolean shouldResize() {
        double avgBucketSize = 1.0 * size.get() / table.length;
        return avgBucketSize > bucketSizeThreshold;
    }
}
