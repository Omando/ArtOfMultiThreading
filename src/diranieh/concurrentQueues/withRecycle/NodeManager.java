package diranieh.concurrentQueues.withRecycle;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Dynamic memory management for linked list nodes
 *
 * @param <E> the type of elements in this list
 */
public class NodeManager<E> {

    // Node recycling: The node stored in this thread local might point to another node
    // and so on. nodeList is therefore a linked list of recyclable nodes
    private ThreadLocal<RecyclableNode<E>> nodeList = ThreadLocal.withInitial(() -> null);

    // A node is being enqueued. Dequeue the head from the linked list of recycled nodes
    // Before: S -> F -> A -> X
    // After calling allocateNode(G):  F -> A -> X. Node S is returned after it gets allocated a value of G
    RecyclableNode<E> allocateNode(E item) {
        // Attempt to recycle a node from the thread local, if available
        RecyclableNode<E> node = nodeList.get();

        // Create and return a new node if no node is available to recycle,
        if (node == null)
            return new RecyclableNode<>(item);

        // A node is available to recycle (call it S). Before returning S, S.next must
        // now replace S in nodeList (see comments for this function).
        RecyclableNode<E> next = node.next.get(new int[1]);   // if node was the only one, next will be null
        nodeList.set(next);

        // Recycle node assigning it the requested value
        node.item = item;
        return node;
    }

    // A node has been dequeued. Prepend it to the other dequeued nodes
    // Before: F -> A -> X
    // After calling free(S): S -> F -> A -> X
    void free(RecyclableNode<E> newlyDequeued) {
        // Get the first recyclable node
        RecyclableNode<E> first = nodeList.get();

        // Prepend the newly dequeued node to the first node
        newlyDequeued.next = new AtomicStampedReference<>(first, 0);

        // The newly dequeued node is now the first one followed by
        // all other dequeued nodes
        nodeList.set(newlyDequeued);
    }
}
