package diranieh.blockingsync.reentrantlock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.*;

class SimpleReentrantLockTest {
    /*  Invariants and post conditions */
    @Test
    public void has_no_locks_on_construction() {
        // Arrange and act
        SimpleReentrantLock lock = new SimpleReentrantLock();

        // Assert
        assertEquals(0, lock.getHoldCount());
        assertEquals(-1, lock.getOwnerId());
    }

    @Test
    public void when_locked_inner_state_updated() {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();

        // Act
        lock.lock();

        // Assert
        assertEquals(1, lock.getHoldCount());
        assertEquals(Thread.currentThread().getId(), lock.getOwnerId());
    }

    @Test
    public void when_locked_and_unlocked_inner_state_reset() {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();

        // Act
        lock.lock();
        lock.unlock();

        // Assert
        assertEquals(0, lock.getHoldCount());
        assertEquals(-1, lock.getOwnerId());
    }

    /* Locking behavior */
    @Test
    public void should_lock_unlock() {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();

        // Act / Assert
        lock.lock();
        assertEquals(1, lock.getHoldCount());
        assertEquals(Thread.currentThread().getId(), lock.getOwnerId());

        lock.unlock();
        assertEquals(0, lock.getHoldCount());
        assertEquals(-1, lock.getOwnerId());
    }

    @Test
    public void when_not_locked_then_unlock_throws() {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();

        IllegalMonitorStateException thrown =assertThrows(IllegalMonitorStateException.class, lock::unlock);
        assertEquals("lock was not called", thrown.getMessage() );
    }

    @Test
    public void when_locked_then_unlock_by_another_thread_throws() throws InterruptedException {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();
        lock.lock();

        // Act
        Thread thread = new Thread(() -> {
            var thrown =assertThrows(IllegalMonitorStateException.class, lock::unlock);

            // assert
            assertEquals("lock not owned by this thread", thrown.getMessage() );
        });
        thread.start();
        thread.join();
    }

    @Test
    public void when_locked_other_threads_should_block() throws InterruptedException {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();
        lock.lock();

        // Act
        Thread thread = new Thread(() -> {
            lock.lock();
            fail("should have blocked");
        });

        thread.start();
        thread.join(1000);

        assertTrue(thread.isAlive());
    }

    @Test
    public void multiple_threads_should_lock_and_unlock() throws BrokenBarrierException, InterruptedException {
        // Arrange
        SimpleReentrantLock lock = new SimpleReentrantLock();
        final int count = 20;
        final CyclicBarrier barrier = new CyclicBarrier(count + 1);     // +1 for this thread
        for(int i = 0; i < count; ++i) {
            Thread thread = new Thread(() -> {
                try {
                    // Wait until all threads are ready
                    barrier.await();

                    doDummyWork(lock);

                    // Wait for all threads to finish
                    barrier.await();

                } catch (Exception e) {
                    System.out.println("[" + Thread.currentThread().getId() + "] thread error: " + e.getMessage());
                }
            });
            thread.start();
        }

        // Act: Start all threads
        barrier.await();

        // Act: Wait for all threads
        barrier.await();

        // Assert. No locks should be held by any thread
        assertEquals(0, lock.getHoldCount());
        assertEquals(-1, lock.getOwnerId());
    }

    private void doDummyWork(SimpleReentrantLock lock) {
        lock.lock();
        lock.lock();        // Reenter

        long threadId = Thread.currentThread().getId();
        System.out.println("[Thread " + threadId + "] Processing");
        assertEquals(threadId, lock.getOwnerId());
        assertEquals(2, lock.getHoldCount());

        double result = 0;
        for (int i = 0; i < 1000; i++) {
            result += Math.sin(i * 3.14159 / 180);
        }
        System.out.println("[Thread " + Thread.currentThread().getId() + "] Completed");

        lock.unlock();
        lock.unlock();
    }

}