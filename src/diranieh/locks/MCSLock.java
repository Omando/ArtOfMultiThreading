package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * MCSLock is very similar to {@link CLHLock}, however MCSLock represents a lock
 * as an explicit linked list of QNode objects, where each QNode represents either
 * a lock holder or a thread waiting to acquire the lock.
 */
public class MCSLock implements Lock {

    // Linked list of nodes
    private static class QNode {
        private boolean locked = false;
        private QNode _next = null;
    }

    AtomicReference<QNode> _tail;  // _queue is an object reference that may be updated atomically
    ThreadLocal<QNode> _myNode;

    public MCSLock() {
        _tail = new AtomicReference<>(null);

        // First node in the linked list is available for the first thread
        _myNode = ThreadLocal.withInitial(() -> new QNode());
    }

    @Override
    public void lock() {
        // Get this thread's QNode and append it at the end of the list
        QNode qNode = _myNode.get();
        QNode predecessor = _tail.getAndSet(qNode);

        // If predecessor is null then this thread is the first thread to acquire the lock.
        // As there is no predecessor, this thread acquires the lock immediately
        if (predecessor == null)
            return;

        // We have a predecessor. Wait until the predecessor sets our locked field to false
        qNode.locked = true;            // We want to acquire the lock
        predecessor._next = qNode;      // predecessor points to this thread's qnode
        while (qNode.locked) {}
    }

    @Override
    public void unlock() {
        // Get this thread's QNode
        QNode node = _myNode.get();

        // Check this thread's qnode _next field:If null, then either no other thread is contending
        // for the lock, or there is another thread, but it is slow
        if (node._next == null) {
            // If the CAScall succeeds, then no other thread is trying to acquire the lock, tail is set
            // to null, and the method returns.
            if (_tail.compareAndSet(node, null))
                return;

            // Otherwise, another (slow) thread is trying to acquire the lock, so the method spins waiting
            // for it to finish
            while (node._next == null) {}
        }

        // Once the successor has appeared, the unlock() method sets its successor’s locked field
        //to false, indicating that the lock is now free
        node._next.locked = false;
        node._next = null;
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
