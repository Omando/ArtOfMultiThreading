package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An unbounded non-blocking concurrent queue implemented using a linked list
 *
 * Recall the characteristics of all nonblocking algorithms: some work is done
 * speculatively and may have to be redone.
 *
 * The overall operation of the lock-free enqueue is as follows:
 *  1. Create a new node and locate the last node in the queue.
 *  2. A call to compareAndSet changes the next field of the node referenced by the
 *  queue’s tail from null to the new node.
 *  3. A call to CompareAndSet changes the queue’s tail from the prior last node to the
 *  current last node.
 *
 *  Because the last two steps are not executed atomically, every other method call
 *  must be prepared to encounter a half-finished enqueue call, and to finish the job
 *
 * @param <E> the type of elements in this list
 */
public class UnboundedConcurrentLockFreeQueue<E> implements Queue<E>  {

    // To use CompareAndSet on a field, the field must be declared using one of AtomicX types.
    // To support calling CompareAndSet on next, declare it as AtomicReference<Node<E>>
    private static class Node<E> {
        private E item;
        private AtomicReference<Node<E>> next;

        public Node(E item) {
            this(item, null);
        }

        public Node(E item, Node<E> next) {
            this.item = item;
            this.next = new AtomicReference<>(next);
        }
    }

    private AtomicReference<Node<E>> head;
    private AtomicReference<Node<E>> tail;

    public UnboundedConcurrentLockFreeQueue() {
        // Create a sentinel node whose value is meaningless and points to
        // nothing (next = null). Initially, both head and tail point to
        // the sentinel
        Node<E> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail =  new AtomicReference<>(sentinel);
    }

    @Override
    public void enqueue(E element)  {
        // Create a new node with the value to be enqueued
        Node<E> newNode = new Node<>(element);

        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while(true) {
            /* After each result is obtained, another thread my make changes*/
            // Locate the last node in the queue
            Node<E> last = tail.get();
            Node<E> next = last.next.get();

            if (last == tail.get()) {       // Do we have the tail?
                if (next == null) {         // No other threads enqueue an item (but not yet advanced tail)?
                    // So far, last is actually the tail since last.next is null. Attempt to
                    // set last.next to the new node, then move (or advance) tail to the new node
                    if (last.next.compareAndSet(next, newNode)) {
                        tail.compareAndSet(last, newNode);
                        return;
                    }
                } else {
                    // last is not the tail since last.next is now not null
                    // So move tail to next
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    @Override
    public E dequeue()  {
        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while (true) {
            Node<E> first = head.get();             // head points to a sentinel with no meaningful value
            Node<E> successor = first.next.get();   // actual head is pointed to by the sentinal
            Node<E> last = tail.get();              // tail node. See document on why this is needed

            // The next three if statements check if the queue is empty
            if (first == head.get()) {
                if (first == last) {
                    if (successor == null)
                        throw new IllegalStateException("Queue is empty");

                    // tail is behind, try to advance (see document for full explanation)
                    tail.compareAndSet(last, successor);
                } else {
                    // Read value of successor
                    E value = successor.item;

                    // Since we have captured the value of teh successor, We 'dequeue' the
                    // successor by making it the sentinel node (recall that we do not care
                    // about the value of the sentinel)
                    if (head.compareAndSet(first, successor))
                        return value;
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        Node<E> first = head.get();
        Node<E> last = tail.get();
        Node<E> next = first.next.get();
        return (first == head.get() && first == last && next == null);
    }
}
