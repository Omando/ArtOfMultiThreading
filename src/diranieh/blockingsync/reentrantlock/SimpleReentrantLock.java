package diranieh.blockingsync.reentrantlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SimpleReentrantLock implements Lock {
    private final Object lock;
    private long ownerId = -1;
    private long holdCount = 0;

    public SimpleReentrantLock() {
        lock = new Object();
    }

    @Override
    public void lock() {
        long threadId = Thread.currentThread().getId();     // local variable. No sync needed :)
        synchronized (lock) {
            if (ownerId == threadId) {
                holdCount++;
                return;
            }
            while (!isOwner()) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) { /* Ignore exception and retry to acquire lock*/ }
            }

            ownerId = threadId;
            holdCount++;
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        long threadId = Thread.currentThread().getId();     // local variable. No sync needed :)
        synchronized (lock) {
            if (ownerId == threadId) {
                holdCount++;
                return;
            }
            while (!isOwner()) {
                lock.wait();
            }

            ownerId = threadId;
            holdCount++;
        }
    }

    @Override
    public void unlock() {
        synchronized (lock) {
            if (holdCount == 0)
                throw new IllegalMonitorStateException("lock was not called");

            if (ownerId != Thread.currentThread().getId())
                throw new IllegalMonitorStateException("lock not owned by this thread");

            --holdCount;
            if (holdCount == 0) {
                ownerId = -1;
                lock.notifyAll();
            }
        }
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /* Package-private visibility for testing*/
    long getOwnerId() {
        return ownerId;
    }

    long getHoldCount() {
        return holdCount;
    }

    /* Implementation details */
    private boolean isOwner() {     // condition queue predicate
        return ownerId == -1 || ownerId == Thread.currentThread().getId();
    }
}
