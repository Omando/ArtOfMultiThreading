package diranieh.linkedlistLocking;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class NonBlockingConcurrentSet<E> implements Set<E> {
    private static class Node<E> {
        private final E item;
        private final int hashCode;
        private AtomicMarkableReference<Node<E>> next;

        public Node(E item, int hashCode) {
            this(item, hashCode, null);
        }

        public Node(E item, int hashCode, AtomicMarkableReference<Node<E>> next) {
            this.item = item;
            this.hashCode = hashCode;
            this.next = next;
        }
    }

    // SearchResult<E> class factors out functionality common to add and remove methods
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
    }

    @Override
    public boolean add(E item) {
        int hashCode = item.hashCode();
        while (true) {
            SearchResult<E> find = search(sentinelHead, item.hashCode());
        }
    }

    @Override
    public boolean remove(E item) {
        return false;
    }

    @Override
    public boolean contains(E item) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

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
