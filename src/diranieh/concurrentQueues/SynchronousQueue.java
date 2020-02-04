package diranieh.concurrentQueues;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueue<E> implements Queue<E> {
    private E item;
    private final Lock lock;
    private final Condition condition;
    private boolean enqueuing;

    public SynchronousQueue() {
        enqueuing = false;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    @Override
    public void enqueue(E element) throws InterruptedException {
        lock.lock();
        try {
            // A: Wait for another thread - if any - to complete enqueuing
            // (see point C below)
            while (enqueuing)
                condition.await();

            // We can proceed: Block other threads from enqueuing, enqueue an item and
            // let a consumer pick it up
            enqueuing = true;
            item = element;
            condition.signalAll();

            // Wait while the item has not been processed by a consumer
            while (item != null)
                condition.await();

            // C: Our item has been picked up by a producer. Let other threads
            // enqueue (a thread waiting at A above can now proceed)
            enqueuing = false;
            condition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public E dequeue() throws InterruptedException {
        E result = null;
        lock.lock();
        try {
            // Wait for a producer to enqueue an item
            while (item == null)
                condition.await();

            // Get the enqueued item
            result = item;

            // Item has been processed. Let the enqueuer know
            item = null;
            condition.signalAll();
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return item == null;
    }
}
