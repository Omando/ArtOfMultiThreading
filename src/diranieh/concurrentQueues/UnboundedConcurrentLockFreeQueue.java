package diranieh.concurrentQueues;

public class UnboundedConcurrentLockFreeQueue<E> implements Queue<E>  {
    @Override
    public void enqueue(E element) throws InterruptedException {

    }

    @Override
    public E dequeue() throws InterruptedException {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
