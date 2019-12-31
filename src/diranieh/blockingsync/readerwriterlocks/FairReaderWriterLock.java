package diranieh.blockingsync.readerwriterlocks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple implementation of {@link ReadWriteLock} supporting similar
 * semantics to {@link ReentrantLock}.
 *
 * <p>This class does not impose a reader or writer preference
 * ordering for lock access. This class does not support upgrading
 * a read lock to a write lock, or downgrading a write lock to a
 * read lock
 *
 * This class ensures that once a writer thread calls gerWriterLock().lock(),
 * irrespective of whether gerWriterLock().lock() acquires the lock or blocks,
 * then no more readers will be able to acquire a read lock until the writer
 * has acquired and released the write lock. Eventually, the readers holding
 * a lock will drain out without letting any more readers in, and the writer
 * thread will acquire the write lock
 */
public class FairReaderWriterLock implements ReaderWriterLock {
    private int readerCount = 0;
    private int writerCount = 0;
    private final Lock lock = new ReentrantLock();
    private final Lock readerLock = new ReaderLock();
    private final Lock writerLock = new WriterLock();
    private final Condition noWriter = lock.newCondition();
    private final Condition noReaders = lock.newCondition();

    private class ReaderLock implements Lock {

        @Override
        public void lock() {
            lock.lock();
            try {
                // Release lock and block while a writer is active. Catch any interrupt and loop again
                while (hasWriter()) {
                    try {
                        noWriter.await();
                    }
                    catch (InterruptedException ignored) { /* Catch the interrupt and wait again */}
                }

                // We can have multiple readers
                ++readerCount;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            lock.lock();
            try {
                // Release lock and block while a writer is active
                // InterruptedException, if any, is rethrown to caller
                while (hasWriter())
                    noWriter.await();

                // We can have multiple readers
                ++readerCount;

            } finally {
                lock.unlock();
            }
        }

        @Override
        public void unlock() {
            lock.lock();
            try {
                if (!hasReaders()) throw new IllegalStateException("Cannot unlock. No locking readers active");

                // Signal only if ALL readers are done
                --readerCount;
                if (!hasReaders())
                    noReaders.signalAll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Condition newCondition() { return lock.newCondition();}

        /* To do */
        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException("To be implemented");
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("To be implemented");
        }
    }

    private class WriterLock implements Lock {

        @Override
        public void lock() {
            lock.lock();
            try {
                // Release lock and block while a writer is active. Catch any interrupt and loop again
                while (hasWriter()) {
                    try {
                        noWriter.await();
                    } catch (InterruptedException ignored) { /* Catch the interrupt and wait again */ }
                }

                // A writer is active
                ++writerCount;

                // Release lock and block while readers are active. Because writerCounter was incremented above
                // any readers attempting to acquire the lock will now block waiting for the the writer lock
                // to unlock. In the meantime, this thread will block if there are any active readers until
                // they unlock. Catch any interrupt and loop again
                while (hasReaders()) {
                    try {
                        noReaders.await();
                    } catch (InterruptedException ignored) { /* Catch the interrupt and wait again */ }
                }

            } finally {
                lock.unlock();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            lock.lock();
            try {
                // Release lock and block while a writer is active
                // InterruptedException, if any, is rethrown to caller
                while (hasWriter())
                    noWriter.await();

                // A writer is active.
                ++writerCount;

                // Release lock and block while readers are active. Because writerCounter was incremented above
                // any readers attempting to acquire the lock will now block waiting for the the writer lock
                // to unlock. In the meantime, this thread will block if there are any active readers until
                // they unlock.InterruptedException, if any, is rethrown to caller
                while (hasReaders())
                    noReaders.await();

            } finally {
                lock.unlock();
            }
        }

        @Override
        public void unlock() {
            lock.lock();
            try {
                if (!hasWriter()) throw new IllegalStateException("Cannot unlock. No locking writer active");

                --writerCount;
                noWriter.signalAll();       // Writer is done
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Condition newCondition() { return lock.newCondition();}

        /* To do */
        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException("To be implemented");
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("To be implemented");
        }
    }

    @Override
    // Delegates implementation of a reader lock to ReaderLock. All threads will get
    // the same instance of the lock
    public Lock getReaderLock() {
        return readerLock;
    }

    @Override
    // Delegates implementation of a reader lock to WriterLock. All threads will get
    // the same instance of the lock
    public Lock gerWriterLock() {
        return writerLock;
    }

    boolean hasReaders() {
        return readerCount > 0;
    }

    boolean hasWriter() {
        return writerCount == 1;
    }
}
