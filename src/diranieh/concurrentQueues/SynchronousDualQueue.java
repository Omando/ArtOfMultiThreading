package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A nonblocking lock-free queue implemented as a linked list of nodes. If the
 * queue is not empty, a node represents either an item waiting to be dequeued,
 * or a reservation waiting to be fulfilled. A node’s type field indicates which.
 *
 *  Reservations (or placeholders) from a dequeuer work as follows: a dequeuer puts
 *  a reservation (placeholder) object in the queue (a node whose item is null),
 *  indicating that the dequeuer is waiting for an enqueuer to fill the placeholder
 *  with an item so that both enqueuer and dequeuer can rendezvous with each other.
 *  The dequeuer then spins on a flag in the  reservation. Later, when this enqueuer
 *  discovers the reservation, it fulfills the reservation by depositing an item
 *  in the placeholder and notifying the dequeuer by setting the reservation's flag
 *
 * At any one time, all existing queue nodes have the same type: either the queue
 * consists entirely of items waiting to be dequeued, or entirely of reservations
 * waiting to be  fulfilled.
 *
 * @param <E>
 */
public class SynchronousDualQueue<E> implements Queue<E> {
    // To support calling CompareAndSet on next, declare it as AtomicReference<Node<E>>
    private enum NodeType {ITEM, RESERVATION}
    private static class Node<E> {
        private NodeType type;
        private AtomicReference<E> item;
        private AtomicReference<Node<E>> next;

        public Node(E item, NodeType type) {
            this(item, type, null);
        }

        public Node(E item, NodeType type, Node<E> next) {
            this.item = new AtomicReference<>(item);
            this.type = type;
            this.next = new AtomicReference<>(next);
        }

        public String toString() {
            return String.format("Node[Type = %1s, Item = %2s, Next = %3s]", type, item, next);
        }
    }

    private AtomicReference<Node<E>> head;
    private AtomicReference<Node<E>> tail;

    public SynchronousDualQueue() {
        // Create a sentinel node whose value is meaningless and points to
        // nothing (next = null). Initially, both head and tail point to
        // the sentinel
        Node<E> sentinel = new Node<>(null, NodeType.ITEM);
        head = new AtomicReference<>(sentinel);
        tail =  new AtomicReference<>(sentinel);
    }

    @Override
    public void enqueue(E element) {

        // We want to enqueue a new item. Type is ITEM
        Node<E> offer = new Node<>(element, NodeType.ITEM);

        // Non-blocking using CompareAndSet requires a loop to keep on
        // trying until successful
        while (true) {
            Node<E> first = head.get();    // head points to a sentinel with no meaningful value
            Node<E> last = tail.get();     // tail node. See document on why this is needed

            // Check whether the queue is empty or whether it contains an enqueued
            // item waiting to be dequeued
            if (first == last || last.type == NodeType.ITEM) {
                Node<E> tailNext = last.next.get();

                // Check if tail values are consistent
                if (last == tail.get()) {
                    // If the tail field does not refer to the last node, advance the
                    // tail field, and start over
                    if (tailNext != null) {
                        tail.compareAndSet(last, tailNext);
                    }

                    // The tail field refers to the last node, so try to append the new
                    // node to the end of the queue such that last.next = the new node
                    else if (last.next.compareAndSet(tailNext, offer)) {
                        // New node appended, try to advance the tail to the new node
                        tail.compareAndSet(last, offer);

                        // Now spin waiting for the dequeuer to signal that it has dequeued
                        // the item by setting the node's item to null
                        while (offer.item.get() == element) ; // spin!!

                        // Item has been dequeued by a dequeuer; clean up by making the node
                        // the new sentinel
                        Node<E> h = head.get();
                        if (offer == h.next.get()) {
                            head.compareAndSet(h, offer);
                        }
                        return;
                    }
                }
            } else {
                // The queue is not empty and there is no item to be dequeued.
                // Determine if we have an inconsistent (transient) snapshot
                Node<E> next = first.next.get(); // read the head's success since the head is a sentinal
                if (last != tail.get() || first != head.get() || next == null)
                    continue;

                // The queue contains a reservation from a dequeuer waiting to be fulfilled,
                // with the reservation's item value equal to null. This means that the dequeuer
                // is waiting for the enqueuer to place a value in that placeholder so that the
                // dequeuer can dequeue it
                // Compare data of the reserved node to null, and if equal replace with the
                // item the user wants to enqueue
                boolean success = next.item.compareAndSet(null, element );
                head.compareAndSet(first, next);
                if (success)
                    return;
            }
        }
    }

    @Override
    public E dequeue()  {
        // We want to create a reservation (placeholder)
        Node<E> offer = new Node<>(null, NodeType.RESERVATION);

        // Non-blocking using CompareAndSet requires a loop to keep on
        // trying until successful
        while (true) {
            Node<E> h = head.get();    // head points to a sentinel with no meaningful value
            Node<E> t = tail.get();    // tail node. See document on why this is needed

            // Check whether the queue is empty or whether it already contains a reservation
            // item waiting to be dequeued
            if (h == t || t.type == NodeType.RESERVATION) {
                Node<E> n = t.next.get();

                // Check if tail values are consistent
                if (t == tail.get()) {

                    // If the tail field does not refer to the last node (because tail.next
                    // is not null), advance the tail field to tail.next and start over
                    if (n != null) {
                        tail.compareAndSet(t, n);
                    }

                    // The tail field refers to the last node, so try to append the new
                    // reservation (placeholdeR) to the end of the queue such that
                    // tail.next = the new node
                    else if (t.next.compareAndSet(n, offer)) {
                        // Reservation node appended, try to advance the tail to the new node
                        tail.compareAndSet(t, offer);

                        // Now spin waiting for the enqueuer to signal that it has enqueued
                        // an item by setting the node's item to a non-null value

                        while (offer.item.get() == null); // spin!!

                        // Item has been enqueued; clean up by making the node the new sentinel
                        h = head.get();
                        if (offer == h.next.get()) {
                            head.compareAndSet(h, offer);
                        }
                        return offer.item.get();
                    }
                }
            } else {
                // The queue is not empty and there is no item to be dequeued.
                // Determine if we have an inconsistent (transient) snapshot
                Node<E> n = h.next.get();
                if (t != tail.get() || h != head.get() || n == null) {
                    continue; // inconsistent snapshot
                }

                // The queue contains an item from an enqueuer waiting to be dequeued.
                // Get the enqueued item (to be returned as value of this function) and
                // CAS item with null to tell the enqueuer that the dequeuer has processed it.
                // We then return the item's value as the dequeued value
                E item = n.item.get();
                boolean success = n.item.compareAndSet(item, null);
                head.compareAndSet(h, n);
                if (success) {
                    return item;
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return head.get() == tail.get();
    }
}
