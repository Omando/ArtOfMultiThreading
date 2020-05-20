package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * ALock is a cyclic queue-based lock
 *
 * ALock improves on {@link BackoffLock} because it reduces invalidations to a minimum
 * and minimizes the interval between when a lock is freed by one thread and when it is
 * acquired by another.
 *
 * The ALock lock is not space efficient; it requires a known bound n on the maximum number
 * of concurrent threads, and it allocates an array of that size per lock. Synchronizing
 * N distinct object requires O(Ln) space
 */
public class ALock implements Lock {
    private final AtomicInteger _tail;      // index of an available slot
    private final int _size;                // array size
    private volatile boolean[] _flag;       // if _flag[j] is true, then a thread with slot j can acquire the lock
    private static ThreadLocal<Integer> _mySlotIndex;   // Maintain slot index for each thread

    public ALock(int capacity) {
        _size = capacity;
        _tail = new AtomicInteger(0);
        _flag = new boolean[capacity];
        _mySlotIndex = ThreadLocal.withInitial(() -> 0);    // First thread has slot 0
        _flag[0] = true;                // on startup, first thread can acquire the lock
    }

    // To acquire a lock, a thread get its slot index and spins until the flag at its slot is true
    @Override
    public void lock() {
        // Get this thread's slot in the _flag array and cache it. The array is cyclic
        // since we are using the modulus operator
        int mySlot = _tail.getAndIncrement() % _size;
        _mySlotIndex.set(mySlot);     // Save this thread's slot. Slot to be retrieved when unlocking

        // Spin while the slot identified by mySlot is not available:
        //  _flag[mySlot] = true --> slot is available
        //  _flag[mySlot] = false --> slot is not available
        while (!_flag[mySlot]) { /* Spin */ }
    }

    @Override
    public void unlock() {
        // Get this thread's slot
        int mySlot =  _mySlotIndex.get();

        // Reset this slot since _flag array is cyclic (see figures in page 5 in java concurrency
        // part 3 doc)
        _flag[mySlot] = false;

        // The next thread to call lock will get a slot whose value mySlot+1, so we indicate that
        // the array cell at index (mySlot+1) is available
        _flag[(mySlot + 1) % _size] = true;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
