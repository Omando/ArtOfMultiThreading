package diranieh.concurrentQueues;

public interface Queue<E> {
    void enqueue(E element) throws InterruptedException;
    E dequeue() throws InterruptedException;
    boolean isEmpty();
}
