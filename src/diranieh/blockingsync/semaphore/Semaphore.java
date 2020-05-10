package diranieh.blockingsync.semaphore;

/* Implemented by {@link SimpleSemaphore} */
public interface Semaphore {
    void acquire();
    void release();
}
