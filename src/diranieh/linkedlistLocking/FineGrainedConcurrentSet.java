package diranieh.linkedlistLocking;

import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedConcurrentSet<E> implements Set<E> {
    private static class Node<E> {
        private final E item;
        private final int hashCode;
        private final ReentrantLock locker;
        private Node<E> next;

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
    }

    // Sentinels: never added, removed, searched or changed
    private final Node<E> sentinelHead;     // sentinelHead.next is head

    public FineGrainedConcurrentSet() {
        // Key for sentinel to head is the min integer value
        sentinelHead = new Node<>(null, Integer.MIN_VALUE);
    }

    /* Adds an item to the set according to the sort order imposed by the key
     * Iterate the list comparing keys. If a match is found, the item is already present
     * and false is returned. If a node with a higher key is encountered, the item is not
     * in the set. The item is added before the node with the higher key, and true is
     * returned*/
    @Override
    public boolean add(E item) {
        int itemHashCode = item.hashCode();
        SearchResult<E> searchResult = null;

        try {
            // Start from the head
            searchResult = search(itemHashCode);

            boolean found = searchResult.current != null && searchResult.current.hashCode == itemHashCode;
            if (!found) {
                Node<E> newNode = new Node<>(item, itemHashCode);
                searchResult.predecessor.next = newNode;
                newNode.next = searchResult.current;
                return true;
            }

            // Item found, nothing to do
            return false;
        }
        finally {
            if (searchResult.current != null)
                searchResult.current.unlock();
            searchResult.predecessor.unlock();
        }
    }

    @Override
    public boolean remove(E item) {
        int itemHashCode = item.hashCode();
        SearchResult<E> searchResult = null;

        try {
            // Start from the head
            searchResult = search(itemHashCode);

            // If item is found delete it, otherwise, nothing to do
            if (searchResult.current != null && searchResult.current.hashCode == itemHashCode) {
                searchResult.predecessor.next = searchResult.current.next;
                return true;
            }

            // Item not found, so nothing to do
            return false;
        }
        finally {
            if (searchResult.current != null)
                searchResult.current.unlock();
            searchResult.predecessor.unlock();
        }
    }

    @Override
    public boolean contains(E item) {
        int itemHashCode = item.hashCode();
        SearchResult<E> searchResult = null;

        try {
            searchResult = search(itemHashCode);

            return searchResult.current != null &&
                    searchResult.current.hashCode == itemHashCode;
        } finally {
            if (searchResult.current != null)
                searchResult.current.unlock();
            searchResult.predecessor.unlock();
        }
    }

    public boolean isEmpty() {
        return sentinelHead.next == null;
    }

    /* Implementation details*/
    private SearchResult<E> search(int itemHashCode) {
        synchronized (this) {
            // Hand-over-hand locking. Obtaining the lock on both nodes (sentinelHead and
            // sentinelHead.next) must be done atomically, otherwise it possible for
            // sentinelHead to be locked by one thread and sentinelHead.next be locked
            // by another thread!
            sentinelHead.lock();
            if (sentinelHead.next != null)
                sentinelHead.next.lock();
        }

        Node<E> predecessor = sentinelHead;
        Node<E> current = sentinelHead.next;

        // Search for the given item starting from the head
        while (current != null && current.hashCode < itemHashCode) {
            predecessor.unlock();
            predecessor = current;     // current already locked, no need to lock predecessor after assignment
            current = current.next;
            if (current != null)
                current.lock();
        }

        return new SearchResult<>(predecessor, current);
    }
}
