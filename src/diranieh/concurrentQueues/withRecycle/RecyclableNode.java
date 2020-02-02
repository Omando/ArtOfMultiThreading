package diranieh.concurrentQueues.withRecycle;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * A linked list that can be dynamically managed using NodeManager<E>
 *
 * To use CompareAndSet on a field, the field must be declared using one of
 * AtomicX types. To support calling CompareAndSet on next and avoid ABA,
 * declare it as AtomicStampedReference<Node<E>>
 * @param <E> the type of elements in this list
 */
public class RecyclableNode<E> {
    E item;
    AtomicStampedReference<RecyclableNode<E>> next;

    public RecyclableNode(E item) {
        this(item, null);
    }

    public RecyclableNode(E item, RecyclableNode<E> next) {
        this.item = item;
        this.next = new AtomicStampedReference<>(next, 0);
    }
}

