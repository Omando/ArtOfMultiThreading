package diranieh.blockingsync.semaphore;

public interface Semaphore {
    void acquire();
    void release();
}
