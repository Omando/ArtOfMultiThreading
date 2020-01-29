package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicReference;

public class UnboundedConcurrentLockFreeQueue<E> implements Queue<E>  {
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
        Node<E> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail =  new AtomicReference<>(sentinel);
    }

    @Override
    public void enqueue(E element) throws InterruptedException {
        // Create a new node with the value to be enqueued
        Node<E> newNode = new Node<>(element);

        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while(true) {
            /* After each result is obtained, another thread my make changes*/
            // Locate the last node in the queue
            Node<E> last = tail.get();
            Node<E> next = last.next.get();

            // Do we have the tail
            if (last == tail.get()) {
                if (next == null) {
                    // So far, last is actually the tail since last.next is null.
                    // Attempt to set last.next to the new node, then move tail to
                    // the new node
                    if (last.next.compareAndSet(next, newNode))
                        tail.compareAndSet(last, newNode);

                    // All done
                    return;
                } else {
                    // last is not the tail since last.next is now not null
                    // So move tail to next
                    tail.compareAndSet(last, next);
                }
            }
        }
    }

    @Override
    public E dequeue() throws InterruptedException {
        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while (true) {
            Node<E> first = head.get();
            Node<E> next = first.next.get();
            Node<E> last = tail.get();
            if (first == head.get()) {
                if (first == last) {
                    if (next == null)
                        throw new IllegalStateException("Queue is empty");
                    tail.compareAndSet(last, next);
                } else {
                    E value = next.item;
                    if (head.compareAndSet(first, next))
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
