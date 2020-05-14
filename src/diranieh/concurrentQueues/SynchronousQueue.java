package diranieh.concurrentQueues;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* SynchronousQueue<E> implements the following pattern:
*   One or more producer threads produce items to be removed in FIFO order by one or more consumers threads.
*   Producers and consumers rendezvous with each other:
*       A producer that puts an item in the queue blocks until that item is removed by a consumer.
*       A consumers that reads an item from the queue blocks until an item is added by a producer.
*   This design incurs a high synchronization cost. Both enqueuers and dequeuers wake up all waiting threads
* */
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
