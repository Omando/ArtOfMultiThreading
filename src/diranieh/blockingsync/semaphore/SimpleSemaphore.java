package diranieh.blockingsync.semaphore;

/* SimpleSemaphore shows how to implement a basic semaphore.
 Start by identifying the condition predicate; number of available slots must be less
 than total capacity. Condition predicate implies an invariant which is totalCapacity and
 a used-slot counter that is incremented/decremented on acquire/release.
 This class has identical structure to  {@link SimpleReentrantLock} class
*/
public class SimpleSemaphore implements Semaphore {
    private final Object lock = new Object();
    private final int totalCapacity;
    private  int slotsUsed;

    public SimpleSemaphore(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    @Override
    public void acquire() {
        synchronized (lock) {
            while( !hasCapacity()) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) { /* Ignore and retry */ }
            }

            ++slotsUsed;
        }
    }

    @Override
    public void release() {
        synchronized (lock) {
            // Check if semaphore was actually acquired
            if (slotsUsed == 0)
                throw new IllegalMonitorStateException("semaphore was not acquired");

            --slotsUsed;

            // One space available for any waiting thread
            lock.notifyAll();
        }
    }

    // Condition queue predicate
    private boolean hasCapacity() {
        return slotsUsed < totalCapacity ;
    }

    /* Used for testing */
    int getTotalCapacity() {
        return totalCapacity;
    }

    int getSlotsUsed() {
        return slotsUsed;
    }
}
