package diranieh.linkedlistLocking;

import java.util.concurrent.locks.ReentrantLock;

public class LazyConcurrentSet<E> implements Set<E> {
    private static class Node<E> {
        private final E item;
        private final int hashCode;
        private final ReentrantLock locker;
        private Node<E> next;
        private boolean isMarked;       // false: reachable, true: unreachable

        public Node(E item, int hashCode) {
            this(item, hashCode, null);
        }

        public Node(E item, int hashCode, Node<E> next) {
            this.item = item;
            this.hashCode = hashCode;
            this.next = next;
            this.locker = new ReentrantLock();
        }

        private void lock() {
            locker.lock();
        }

        private void unlock() {
            locker.unlock();
        }
    }

    // Holds the result of calling SimpleSet<E>.search
    private static class SearchResult<E> {
        private final Node<E> predecessor;
        private final Node<E> current;

        public SearchResult(Node<E> predecessor, Node<E> current) {
            this.predecessor = predecessor;
            this.current = current;
        }

        void lock() {
            predecessor.lock();
            if (current != null)
                current.lock();
        }

        void unlock() {
            predecessor.unlock();
            if (current != null)
                current.unlock();
        }
    }

    // Sentinels: never added, removed, searched or changed
    private final Node<E> sentinelHead;     // sentinelHead.next is head

    public LazyConcurrentSet() {
        this.sentinelHead = new Node<>(null, Integer.MIN_VALUE);
    }

    @Override
    public boolean add(E item) {
        int itemHashCode = item.hashCode();

        while (true) {
            // Search for an item with the same hash code
            SearchResult<E> searchResult = search(itemHashCode);

            try {
                // Lock the nodes just found while we validate
                searchResult.lock();

                // Validate the nodes found
                if (isValidated(searchResult.predecessor, searchResult.current)) {

                    // We have valid nodes. Nothing to do if item was found, else add the item
                    // between predecessor and current
                    if (searchResult.current != null && searchResult.current.hashCode == itemHashCode)
                        return false;
                    else {
                        Node<E> newNode = new Node<>(item, itemHashCode);
                        searchResult.predecessor.next = newNode;
                        newNode.next = searchResult.current;
                        return true;
                    }
                }
            } finally {
                searchResult.unlock();
            }
        }
    }

    @Override
    public boolean remove(E item) {
        int itemHashCode = item.hashCode();

        while (true) {
            // Search for an item with the same hash code
            SearchResult<E> searchResult = search(itemHashCode);

            try {
                // Lock the found items while we validate
                searchResult.lock();

                // Validate the nodes found
                if (isValidated(searchResult.predecessor, searchResult.current)) {

                    // We have valid nodes. If item was found, remove the item pointed to by
                    // predecessor (i.e., current), else nothing to do
                    if (searchResult.current != null && searchResult.current.hashCode == itemHashCode) {
                        searchResult.current.isMarked = true;
                        searchResult.predecessor.next = searchResult.current.next;
                        return true;
                    }
                    return false;
                }
            } finally {
                searchResult.unlock();
            }
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

    // predecessor and current nodes are validated if both are NOT marked,
    // and predecessor points to current
    private boolean isValidated(Node<E> predecessor, Node<E> current) {
        return !predecessor.isMarked &&         // is predecessor reachable?
                !current.isMarked &&            // is current reachable?
                predecessor.next == current;    // does predecessor point to current?
    }

    private SearchResult<E> search(int itemHashCode) {
        // Start from the head
        Node<E> predecessor = sentinelHead;
        Node<E> current = predecessor.next;

        while(current != null && current.hashCode < itemHashCode) {
            predecessor = current;
            current = current.next;
        }

        return new SearchResult<>(predecessor, current);
    }
}
