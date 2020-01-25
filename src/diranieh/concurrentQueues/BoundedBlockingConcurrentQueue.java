package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A bounded concurrent queue implemented using a linked list
 * @param <E> the type of elements in this list
 */
public class BoundedBlockingConcurrentQueue<E> implements Queue<E>  {
    private static class Node<E> {
        private E item;
        private Node<E> next;

        public Node(E item) {
            this(item, null);
        }

        public Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    private final ReentrantLock enqLock;
    private final Condition notFullCondition;     // condition predicate for enqLock used by enqueue
    private final ReentrantLock deqLock;
    private final Condition notEmptyCondition;    // condition predicate for deqLock used by dequeue
    private final int capacity;
    private final AtomicInteger size;
    private volatile Node<E> head;              // why volatile?
    private volatile Node<E> tail;              // why volatile?

    public BoundedBlockingConcurrentQueue(int capacity) {
        this.capacity = capacity;
        enqLock = new ReentrantLock();
        deqLock = new ReentrantLock();
        notFullCondition = enqLock.newCondition();
        notEmptyCondition = deqLock.newCondition();
        size = new AtomicInteger(0);
        head = new Node<>(null);
        tail = head;

    }

    // Condition predicate that must be met is: 'queue is not full'.
    // enqueue method  must wait while condition predicate is not met, or in other words,
    // enqueue method  must wait while 'queue is not full' is not true
    @Override
    public void enqueue(E element) {
        boolean shouldWakeDequeuers = false;
        enqLock.lock();
        try {
            // Release lock and while the queue is full
            while (!isNotFull()) {          // or, while (isFull)
                notFullCondition.await();
            }

            // Queue has space: add new item to end of queue (easily understood by drawing
            // a linked list, labelling the current tail, pointing the current tail to the
            // new node, and finally labelling the new node as the new tail!)
             Node<E> newNode = new Node<>(element);
             tail.next = newNode;
             tail = newNode;

             // New item added so increment size
             int previousSize = size.getAndIncrement();

             // Dequeuers will be blocked only if queue was empty. Wake all waiting dequeuers
            // if queue transitions from empty to non-empty
            if (previousSize == 0)
                shouldWakeDequeuers = true;

        } catch (InterruptedException ignored) {
        } finally {
            enqLock.unlock();
        }

        // Placing this block of code inside the previous try block after checking
        // if previouseSize == 0 would cause a deadlock, hence it is after enqLock
        // was released
        if (shouldWakeDequeuers) {
            deqLock.lock();
            try {
                notEmptyCondition.signalAll();
            } finally {
                deqLock.unlock();
            }
        }
    }

    @Override
    public E dequeue() {
        E dequeuedResult = null;
        boolean shouldWakeEnqueuers = false;
        deqLock.lock();
        try {
            // Release lock and while the queue is empty
            while (!isNotEmpty()) {          // or, while (isEmpty)
                notEmptyCondition.await();
            }

            // Queue is now not empty: remove item from the head of the queue
            dequeuedResult = head.item;
            head = head.next;

            // Existing item removed so decrement size
            int previousSize = size.getAndDecrement();

            // Enqueuers will be blocked only if queue was full. Wake all waiting enqueuers
            // if queue transitions from full to not-full
            if (previousSize == capacity)
                shouldWakeEnqueuers = true;
        } catch (InterruptedException ignored) {
        } finally {
            deqLock.unlock();
        }

        // Placing this block of code inside the previous try block after checking
        // if previouseSize == 0 would cause a deadlock, hence it is after enqLock
        // was released
        if (shouldWakeEnqueuers) {
            enqLock.lock();
            try {
                notFullCondition.signalAll();
            } finally {
                enqLock.unlock();
            }
        }

        return dequeuedResult;
    }

    private boolean isNotFull() {
        return size.get() < capacity;
    }

    private boolean isNotEmpty() {
        return size.get() > 0;
    }
}
