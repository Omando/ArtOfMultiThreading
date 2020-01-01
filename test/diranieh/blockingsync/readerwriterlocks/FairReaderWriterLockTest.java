package diranieh.blockingsync.readerwriterlocks;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

//class FairReaderWriterLockTest {  }
class FairReaderWriterLockTest {
    /*  Invariants and post conditions */
    @Test
    public void has_no_readers_or_writers_on_construction() {
        // Arrange and act
        FairReaderWriterLock readerWriterLock = new FairReaderWriterLock();

        // Assert
        assertFalse(readerWriterLock.hasReaders());
        assertFalse(readerWriterLock.hasWriter());
    }

    @Test
    public void should_have_reader_and_no_writer() {
        // Arrange
        FairReaderWriterLock readerWriterLock = new FairReaderWriterLock();

        // Act
        Lock readerLock = readerWriterLock.getReaderLock();
        readerLock.lock();

        // Assert
        assertTrue(readerWriterLock.hasReaders());
        assertFalse(readerWriterLock.hasWriter());

        // Release lock
        readerLock.unlock();
    }

    @Test
    public void should_have_writer_and_no_reader() {
        // Arrange
        FairReaderWriterLock readerWriterLock = new FairReaderWriterLock();

        // Act
        Lock writerLock = readerWriterLock.gerWriterLock();
        writerLock.lock();

        // Assert
        assertTrue(readerWriterLock.hasWriter());
        assertFalse(readerWriterLock.hasReaders());

        // Release lock
        writerLock.unlock();
    }

    /* Locking behavior */
    @Test
    public void should_block_writer_when_readers_active() throws InterruptedException {
        /* Arrange */
        FairReaderWriterLock readerWriteLock = new FairReaderWriterLock();
        Lock readerLock = readerWriteLock.getReaderLock();
        readerLock.lock();

        Thread writerThread = new Thread(() -> {
            try {
                Lock writerLock = readerWriteLock.gerWriterLock();
                writerLock.lockInterruptibly();      // should block until interrupted
                fail("Got a writer lock when a reader lock was active");
            } catch (InterruptedException e) {
                System.out.println("writer thread interrupted");
            }
        });

        /* Act */
        // Start the writer thread and wait for it to get scheduled
        writerThread.start();
        Thread.sleep(500);

        // By now, writerThread should have been scheduled and getWriterLock() blocked.
        // Trigger the catch block which causes the thread to exit
        writerThread.interrupt();
        writerThread.join();

        // Assert
        assertFalse(writerThread.isAlive());

        // Release lock
        readerLock.unlock();
    }

    @Test
    public void should_block_reader_and_writer_when_writer_active() throws InterruptedException {
        /* Arrange */
        FairReaderWriterLock readerWriteLock = new FairReaderWriterLock();
        Lock writerLock = readerWriteLock.gerWriterLock();
        writerLock.lock();

        Thread readerThread = new Thread(() -> {
            try {
                Lock readerLock = readerWriteLock.getReaderLock();
                readerLock.lockInterruptibly();      // should block until interrupted
                fail("Got a reader lock when a writer lock was active");
            } catch (InterruptedException e) {
                System.out.println("reader thread interrupted");
            }
        });

        Thread writerThread = new Thread(() -> {
            try {
                Lock writerLock2 = readerWriteLock.gerWriterLock();
                writerLock2.lockInterruptibly();      // should block until interrupted
                fail("Got a writer lock when a writer lock was active");
            } catch (InterruptedException e) {
                System.out.println("writer thread interrupted");
            }
        });

        /* Act */
        // Start the reader and writer threads and wait for it to get scheduled
        readerThread.start();
        writerThread.start();
        Thread.sleep(500);

        // By now, readerThread and writerThread should have been scheduled and getReaderLock()
        // and getWriterLock() calls blocked. Trigger the catch block in both threads which
        // causes each thread to exit
        readerThread.interrupt();
        writerThread.interrupt();
        readerThread.join();
        writerThread.join();

        // Assert
        assertFalse(readerThread.isAlive());
        assertFalse(writerThread.isAlive());

        // Release writer lock
        writerLock.unlock();
    }

    @Test
    public void should_not_block_reader_when_another_reader_active() throws InterruptedException {
        // Arrange
        FairReaderWriterLock readerWriteLock = new FairReaderWriterLock();
        Lock readerLock1 = readerWriteLock.getReaderLock();
        readerLock1.lock();

        Thread readerThread = new Thread(() -> {
            Lock readerLock2 = readerWriteLock.getReaderLock();
            readerLock2.lock(); // should not block
            doDummyWork("reader2");
            readerLock2.unlock();
        });

        /* Act */
        // Start the reader thread and wait for it to get scheduled
        readerThread.start();
        Thread.sleep(500);

        // readerThread should not block
        readerThread.join(1000);

        // Assert
        assertFalse(readerThread.isAlive());

        // Release reader lock
        readerLock1.unlock();
    }

    // Uses Countdown latch to maximize interleaving between threads
    @Test
    public void should_synchronize_multiple_readers_multiple_writers() {

        // Arrange
        final int readerThreads = 10;
        final int writerThreads = 10;
        final int partyCount = readerThreads + writerThreads + 1;      // +1 for main thread
        CyclicBarrier barrier = new CyclicBarrier(partyCount);
        ExecutorService pool = Executors.newCachedThreadPool();
        FairReaderWriterLock readerWriterLock = new FairReaderWriterLock();

        for (int j = 0; j < writerThreads; j++) {
            Thread thread = new Thread(() -> {
                try {
                    Lock writerLock = readerWriterLock.gerWriterLock();

                    // Wait until all threads are ready to start
                    System.out.println("[Thread " + Thread.currentThread().getId() + "]: writer ready");
                    barrier.await();

                    // Get reader lock (should not block) and do some work
                    writerLock.lock();
                    doDummyWork("writer");
                    writerLock.unlock();

                    // Wait until all threads are finished
                    System.out.println("[Thread " + Thread.currentThread().getId() + "]: writer finished");
                    barrier.await();

                } catch (Exception e) {
                    System.out.println("Writer thread error: " + e.getMessage());
                }
            });
            thread.start();
        }

        // Setup 5 reader threads
        for (int i = 0; i < readerThreads; i++) {

            Thread thread = new Thread(() -> {
                try {
                    Lock readerLock = readerWriterLock.getReaderLock();

                    // Wait until all threads are ready to start
                    System.out.println("[Thread " + Thread.currentThread().getId() + "]: Reader ready" );
                    barrier.await();

                    // Get reader lock (should not block) and do some work
                    readerLock.lock();
                    doDummyWork("reader");
                    readerLock.unlock();

                    // Wait until all threads are finished
                    System.out.println("[Thread " + Thread.currentThread().getId() + "]: reader finished" );
                    barrier.await();

                } catch (Exception e) {
                    System.out.println("Reader thread error: " + e.getMessage());
                }
            });
            thread.start();
        }

        try {
            // Wait for all threads to be ready then start
            barrier.await();

            // Wait for all threads to complete then terminate
            barrier.await();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        assertFalse(readerWriterLock.hasWriter());
        assertFalse(readerWriterLock.hasReaders());
    }

    private void doDummyWork(String type) {
        System.out.println("[Thread " + Thread.currentThread().getId() + "][Processing]: " + type);
        double result = 0;
        for (int i = 0; i < 1000; i++) {
            result += Math.sin(i * 3.14159 / 180);
        }
    }

}