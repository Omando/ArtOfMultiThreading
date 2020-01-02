package diranieh.blockingsync.semaphore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SimpleSemaphoreTest {
    /* Invariants and post conditions */
    @Test
    void should_have_full_capacity_after_construction() {
        // Arrange and act
        final int totalCapacity = 19;
        SimpleSemaphore semaphore = new SimpleSemaphore(totalCapacity);

        assertEquals(totalCapacity, semaphore.getTotalCapacity());
        assertEquals(0, semaphore.getSlotsUsed());
    }

    @Test
    void should_acquire_and_release_slots() {
        // Arrange
        final int totalCapacity = 19;
        SimpleSemaphore semaphore = new SimpleSemaphore(totalCapacity);

        // Act & Asserts
        for(int i = 1; i <= totalCapacity; ++i) {
            semaphore.acquire();
            assertEquals(i, semaphore.getSlotsUsed());
        }

        for(int i = 1; i <= totalCapacity; ++i) {
            semaphore.release();
            assertEquals(totalCapacity - i, semaphore.getSlotsUsed());
        }

        // Assert: state should now be reset
        assertEquals(totalCapacity, semaphore.getTotalCapacity());
        assertEquals(0, semaphore.getSlotsUsed());
    }

    @Test
    void should_block_when_slots_are_unavailable() throws InterruptedException {
        // Arrange
        SimpleSemaphore semaphore = new SimpleSemaphore(1);
        semaphore.acquire();

        // Act: This thread should block
        Thread thread = new Thread(() -> {
            semaphore.acquire();
            fail("Semaphore was acquired");
        });
        thread.start();
        Thread.sleep(100);

        // Assert
        assertEquals(1, semaphore.getSlotsUsed());
    }

    /* Output:
        Main thread acquired semaphore
        Thread attempting to acquire semaphore
        Main thread released semaphore
        Thread acquired semaphore
        Thread releasing semaphore
     */
    @Test
    void should_unblock_when_slots_become_available() throws InterruptedException {
        // Arrange
        SimpleSemaphore semaphore = new SimpleSemaphore(1);
        semaphore.acquire();
        System.out.println("Main thread acquired semaphore");

        // Act: This thread should block
        Thread thread = new Thread(() -> {
            System.out.println("Thread attempting to acquire semaphore");
            semaphore.acquire();
            System.out.println("Thread acquired semaphore");
            semaphore.release();
            System.out.println("Thread releasing semaphore");
        });
        thread.start();
        Thread.sleep(100);

        // Assert: releasing semaphore should unblock the thread
        semaphore.release();
        System.out.println("Main thread released semaphore");
        thread.join();
        assertEquals(0, semaphore.getSlotsUsed());
    }
}