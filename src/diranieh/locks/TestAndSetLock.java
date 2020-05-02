package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/* Simple class to illustrate the concept and test and set */
public class TestAndSetLock implements Lock {
    AtomicBoolean state = new AtomicBoolean(false);

    @Override
    public void unlock() {
        // Setting the state to false means the lock is free
        state.set(false);
    }

    @Override
    public void lock() {
        // A lock is free if the state is false. If getAndSet(true) returns false
        // it means the lock was free and we have acquired it. If getAndSet(true)
        // returns true it  means the lock is held and we have to try again until
        // getAndSet returns false. Therefore, repeatedly apply getAndSet until
        // it returns false (lock is free)
        while (state.getAndSet(true)) {/* Do nothng. Try again*/}
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // TODO
    }

    @Override
    public boolean tryLock() {
        // TODO
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // TODO
        return false;
    }

    @Override
    public Condition newCondition() {
        // TODO
        return null;
    }
}
