package diranieh.concurrentQueues;

import java.util.concurrent.locks.ReentrantLock;

/**
 * An unbounded concurrent queue implemented using a linked list
 * Similar notes from {@link BoundedConcurrentBlockingQueue} apply, except that there
 * are no condition predicates; enqueue always enqueues its item, and dequeue throws
 * an exception if there is no item to dequeue.
 *
 * The representation is the same as {@link BoundedConcurrentBlockingQueue} except there
 * is no need to count the number of items in the queue, or to provide condition queues
 * on which to wait.
 *
 * As usual, the queue's actual head is head.next and the actual tail is tail.next which
 * is also the last item reachable from the head
 *
 * @param <E> the type of elements in this list
 */
public class UnboundedConcurrentBlockingQueue<E> implements Queue<E>   {
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

    private final ReentrantLock enqLock = new ReentrantLock();
    private final ReentrantLock deqLock = new ReentrantLock();
    private volatile Node<E> head;
    private volatile Node<E> tail;

    public UnboundedConcurrentBlockingQueue() {
        head = new Node<>(null);
        tail = head;
    }

    @Override
    public void enqueue(E element) throws InterruptedException {
        enqLock.lockInterruptibly();
        try {
            //Add new item to end of queue (easily understood by drawing a linked list,
            // labelling the current tail, pointing the current tail to the new node,
            // and finally labelling the new node as the new tail!)
            Node<E> newNode = new Node<>(element);
            tail.next = newNode;
            tail = newNode;
        } finally {
            enqLock.unlock();
        }
    }

    @Override
    public E dequeue() throws InterruptedException {
        E dequeuedResult = null;
        deqLock.lockInterruptibly();
        try {
            if (head.next == null)
                throw new IllegalStateException("Queue is empty");

            // Queue is now not empty: remove item from the head of the queue
            dequeuedResult = head.next.item;
            head = head.next;

        } finally {
            deqLock.unlock();
        }

        return dequeuedResult;
    }

    @Override
    public boolean isEmpty() {
        return head.next == null;
    }
}
