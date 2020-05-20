package diranieh.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * The BackoffLock with exponential delayis a refinement of the {@link TestAndTestAndSetLock}
 * lock. Recall that contention occurs when multiple threads try to acquire the same lock at
 * the same time.
 *
 *  A key observation is that if some other thread acquires the lock in between the first and
 *  second tests, then there is high contention for the lock and it is very inefficient to
 *  immediately try to acquire the lock again. Such an attempt contributes to bus traffic
 *  at a time when the chances of acquiring the lock are slim. It is far more efficient for
 *  the thread to back off for some duration allowing other threads to finish first.
 */
public class BackoffLock implements Lock {
    private final AtomicBoolean state = new AtomicBoolean(false);
    private static final int MIN_DELAY = 2;         // 2 msec
    private static final int MAX_DELAY = 64;        // 64 msec

    @Override
    public void lock() {
        // Start with a brand new back off
        ExponentialBackoff expBackoff = new ExponentialBackoff(MIN_DELAY, MAX_DELAY);

        while (true) {
            // First test: spin while lock is already held
            while (state.get()) {/* do nothing */}

            // Second test: If getAndSet returns false then the lock is free and we have
            // successfully acquired it. Otherwise backoff exponentially and start again
            if (!state.getAndSet(true))
                return;

            // Lock was acquired by another thread in between first and second tests
            // Back-off exponentially before trying to acquire the lock again
            try {
                expBackoff.backOff();
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void unlock() {
        state.set(false);   // Setting the state to false means the lock is free
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
