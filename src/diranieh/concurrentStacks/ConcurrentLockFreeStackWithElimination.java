package diranieh.concurrentStacks;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock free stack with elimination
 *
 *
 * @param <E> the type of elements in this array
 */
public class ConcurrentLockFreeStackWithElimination<E> implements Stack<E> {
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

    private final int eliminationCapacity;
    private final EliminationArray<E> eliminationArray;
    private final ThreadLocal<RangePolicy> policy;

    // Sentinels
    private Node<E> head;

    public ConcurrentLockFreeStackWithElimination(int eliminationCapacity) {
        this.eliminationCapacity = eliminationCapacity;
        eliminationArray = new EliminationArray<>(eliminationCapacity);
        policy = ThreadLocal.withInitial(() -> new RangePolicy(eliminationCapacity));

        // Linked list is initially empty
        head = new Node<>(null);
    }

    @Override
    public void push(E item) {
        RangePolicy localPolicy = policy.get();
        Node<E> newNode = new Node<>(item);

        // Use CAS to push an item, therefore, we need an infinite loop until successful
        while (true) {
            // Get current item at head and attempt topush
            Node<E> top = head.next.get();
            newNode.next.set(top);
            if (head.next.compareAndSet(top, newNode))
                return;

            // Unable to push node. Instead of backing off, use the elimination array
            // to exchange value
            try {
                // visit() selects a random array entry within its range and attempts to exchange
                // item with another thread. If the exchange is successful, this thread checks whether
                // the value was exchanged with a pop() by testing if the value exchanged was null.
                // (Recall that pop() always offers null to the exchanger while push() always offers
                // a non-null value
                E otherValue = eliminationArray.visit(item, localPolicy.getRange());
                if (otherValue == null) {
                    // Successfully exchanged with pop()
                    localPolicy.recordEliminationSuccess();
                    return;
                }

            } catch (TimeoutException exception) {
                localPolicy.recordEliminationTimeout();
            }
        }
    }

    @Override
    public E pop() {
        RangePolicy localPolicy = policy.get();

        // Use CAS to pop an item, therefore, we need an infinite loop until successful
        while (true) {
            // Throw exception if stack is empty
            Node<E> top = head.next.get();
            if (top == null)
                return null;

            // Stack not empty, attempt to make the next item the new top item
            Node<E> next = top.next.get();
            if (head.next.compareAndSet(top, next))
                return top.item;

            // Unable to pop a node. Instead of backing off, use the elimination array
            // to exchange value
            try {
                E otherValue = eliminationArray.visit(null, localPolicy.getRange());
                if (otherValue != null) {
                    localPolicy.recordEliminationSuccess();
                    return otherValue;
                }
            } catch (TimeoutException e) {
                localPolicy.recordEliminationTimeout();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return head.next.get() == null;
    }
}
