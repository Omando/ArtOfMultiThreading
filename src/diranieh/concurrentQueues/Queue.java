package diranieh.concurrentQueues;

public interface Queue<E> {
    void enqueue(E element);
    E dequeue();
}
