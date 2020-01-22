package diranieh.linkedlistLocking;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class NonBlockingConcurrentSet<E> implements Set<E> {
    private static class Node<E> {
        private final E item;
        private final int hashCode;
        private AtomicMarkableReference<Node<E>> next;

        public Node(E item, int hashCode) {
            // In previous implementations we passed null for next. Here we
            // pass an AtomicMarkableReference<E> initialized with null and false
            this(item, hashCode, new AtomicMarkableReference<>(null, false));
        }

        public Node(E item, int hashCode, AtomicMarkableReference<Node<E>> next) {
            this.item = item;
            this.hashCode = hashCode;
            this.next = next;
        }
    }

    // A container for predecessor and current nodes
    private static class SearchResult<E> {
        private final Node<E> predecessor;
        private final Node<E> current;

        public SearchResult(Node<E> predecessor, Node<E> current) {
            this.predecessor = predecessor;
            this.current = current;
        }
    }

    // Sentinels: never added, removed, searched or changed
    private final Node<E> sentinelHead;     // sentinelHead.next is head

    public NonBlockingConcurrentSet() {
        this.sentinelHead = new Node<>(null, Integer.MIN_VALUE);

        // Adding tail will simplify code below wihtout having to check that head is null
        Node<E> tail = new Node<>(null, Integer.MAX_VALUE);
        while (!this.sentinelHead.next.compareAndSet(null, tail, false, false));
    }

    @Override
    public boolean add(E item) {
        int hashCode = item.hashCode();
        while (true) {
            SearchResult<E> find = search(sentinelHead, item.hashCode());

            // Nothing to do if the item is already presnet
            if (find.current.hashCode == hashCode)
                return false;

            // Item not found. Attempt to add it between predecessor and current
            Node<E> newNode = new Node<>(item, hashCode);
            newNode.next = new AtomicMarkableReference<>(find.current, false);
            if (find.predecessor.next.compareAndSet(find.current, newNode, false, false))
                return true;

            // Could not add node, so start again searching for item starting from the head
        }
    }

    @Override
    public boolean remove(E item) {
        int hashCode = item.hashCode();
        while (true) {
            SearchResult<E> find = search(sentinelHead, item.hashCode());
            Node<E> predecessor = find.predecessor, current = find.current;

            // Nothing to do if the item is not present
            if (current.hashCode != hashCode)
                return false;

            // Item found. Attempt to delete it by pointing predecessor.next to
            // current.next
            Node<E> successor = current.next.getReference();

            // marking successor as deleted!!??? DO NOT UNDERSTAND THIE LINE
            if (!current.next.attemptMark(successor, true))
                continue;

            // Try to advance reference. If unsuccessful, some other thread already did it
            predecessor.next.compareAndSet(current, successor, false, false);
            return true;
        }
    }

    @Override
    public boolean contains(E item) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    // factors out functionality common to add and remove methods
    private SearchResult<E> search(Node<E> head, int hashCode) {
        Node<E> predecessor, current, successor;
        boolean[] marked = new boolean[1];
        boolean isDeleted;

        // Keep on retrying
        retry: while(true) {
            // get  predecessor and current nodes, starting from the given head
            predecessor = head;
            current = predecessor.next.getReference();

            while(true) {
                // For the current node, get the successor node and the successor's mark
                successor = current.next.get(marked);       // on return marked[0] holds the value of the mark

                // Physically remove marked nodes
                while (marked[0]) {
                    // Attempt to physically remove the marked node.
                    // compareAndSet tests the field's reference the boolean mark values, and fails if
                    // either has changed. A concurrent thread could change the mark value by logically
                    // removing predecessor or it could change the reference value by physically removing
                    // current
                    isDeleted = predecessor.next.compareAndSet(current, successor, false, false);

                    // If compareAndSet fails, then restart traversal from the head of the list, otherwise
                    // the traversal continues
                    if (!isDeleted) continue retry;
                    current = successor;
                    successor = current.next.get(marked);   // on return marked[0] holds the value of the mark
                }

                // All marked nodes (if any) removed by now. Check if we have found the node
                // of interest, otherwise, loop and advance through the list
                if (current.hashCode >= hashCode)
                    return new SearchResult<>(predecessor, current);
                predecessor = current;
                current = successor;
            }
        }
    }
}
