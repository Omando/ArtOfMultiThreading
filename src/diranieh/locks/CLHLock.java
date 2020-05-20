package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * CLHLock records each thread’s status in a QNode object which has a boolean locked
 * field. If locked is true, then the corresponding thread has either acquired the
 * lock or is waiting for the lock, and if false, the thread has released the lock.
 *
 * Each thread refers to its predecessor through a ThreadLocaL<QNode> predecessor
 * variable. This effectively creates a virtual (or implicit) linked list of QNode
 * objects
 *
 * When a thread attempts to acquire a lock, it spins on its predecessor node waiting
 * for the locked field to become false.
 */
public class CLHLock implements Lock {

    private static class QNode {
        private boolean locked = false;
    }

    AtomicReference<QNode> _tail;
    ThreadLocal<QNode> _myNode;
    ThreadLocal<QNode> _myPredecessor;

    public CLHLock() {
        _tail = new AtomicReference<>(new QNode());
        _myNode = ThreadLocal.withInitial(() -> new QNode());
        _myPredecessor = ThreadLocal.withInitial(() -> null);
    }

    @Override
    public void lock() {
        // Get this thread's QNode and set its locked field to true to indicate that it
        // is waiting for the lock (or it is has acquired the lock)
        QNode qNode = _myNode.get();
        qNode.locked = true;

        // Make this thread's node (qNode) the tail of the queue and acquire the predecessor node
        QNode predecessor = _tail.getAndSet(qNode);
        _myPredecessor.set(predecessor);        // remembet the predecessor

        // Wait until the predecessor releases its lock
        while (predecessor.locked) {}
    }

    @Override
    public void unlock() {
        // Get this thread's QNode and set its locked field to false to indicate that it
        // this thread has released the lock
        QNode node = _myNode.get();
        node.locked = false;

        // Reuse the predecessor node for future lock accesses. This can be done because the predecessor
        // node is no longer used by the predecessor thread
        _myNode.set( _myPredecessor.get());
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
