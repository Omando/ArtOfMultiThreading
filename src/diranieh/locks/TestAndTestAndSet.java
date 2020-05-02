package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/* Simple class to illustrate the concept and test and test and set. This class scales
much better than TestAndSet: Assume the lock is held by a thread A. The FIRST TIME thread
B reads the lock it takes a cache miss, forcing B to block while the value is loaded into
B’s cache via the shared memory bus. As long as A holds the lock, B repeatedly rereads the
value, but hits in the cache every time. B thus produces no bus traffic, and does not slow
down other threads’ memory accesses */
public class TestAndTestAndSet implements Lock {
    AtomicBoolean state = new AtomicBoolean(false);

    @Override
    public void unlock() {
        // Setting the state to false means the lock is free
        state.set(false);
    }

    @Override
    public void lock() {

        while (true) {
            // First test
            // A lock is free if the state is false. If get() returns false it means the lock
            // was free and we have acquired it. If get() returns true it  means the lock is
            // held and we have to try again until
            while (state.get()) {/* Do nothing and retry*/ }

            // Second test
            // If getAndSet returns false then the lock is free and we are done. Otherwise
            // start all over again
            if (!state.getAndSet(true))
                return;
        }
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

