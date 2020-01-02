package diranieh.blockingsync.semaphore;

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
            if (slotsUsed == 0)
                throw new IllegalMonitorStateException("semaphore was not acquired");

            --slotsUsed;

            // Allow other threads since we have capacity
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
