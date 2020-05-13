package diranieh.concurrentQueues;

/* Implemented by all queue classes */
public interface Queue<E> {
    void enqueue(E element) throws InterruptedException;
    E dequeue() throws InterruptedException;
    boolean isEmpty();
}
