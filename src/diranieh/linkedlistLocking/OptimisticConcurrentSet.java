package diranieh.linkedlistLocking;

import diranieh.utilities.Set;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A concurrent linked list implementation of a set with optimistic synchronization
 * Synchronization is improved by optimistically acquiring locks:
 *      1. Search without acquiring locks, and if search is successful, lock the relevant nodes
 *         and validate them (explained below).
 *      2. If the locked nodes cannot be validated, release the nodes and repeat the process.
 *
 *  When add/remove/contains methods get called, the search may identify two nodes, predecessor
 *  and current. These nodes must be validated  because the trail of nodes leading to predecessor
 *  or the reference from predecessor to current could have been changed by other threads between
 *  when they were last read by the current thread and when the current thread acquired the lock
 *  on those two nodes.
 *
 * @param <E> the type of elements in this list
 */
public class OptimisticConcurrentSet<E> implements Set<E> {

    private static class Node<E> {

        private final E item;
        private final int hashCode;
        private Node<E> next;
        private final ReentrantLock locker = new ReentrantLock();

        public Node(E item, int hashCode) {
            this(item, hashCode, null);
        }

        public Node(E item, int hashCode, Node<E> next) {
            this.item = item;
            this.hashCode = hashCode;
            this.next = next;
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

    public OptimisticConcurrentSet() {
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
                if (isValidated(searchResult)) {

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
                if (isValidated(searchResult)) {

                    // We have valid nodes. If item was found, remove the item pointed to by
                    // predecessor (i.e., current), else nothing to do
                    if (searchResult.current != null && searchResult.current.hashCode == itemHashCode) {
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
        int itemHashCode = item.hashCode();

        while (true) {
            // Search for an item with the same hash code
            SearchResult<E> searchResult = search(itemHashCode);

            try {
                // Lock the found items while we validate
                searchResult.lock();

                // Validate the nodes found
                if (isValidated(searchResult)) {

                    // We have valid nodes. Check current is what we're after
                    boolean isFound = searchResult.current != null && searchResult.current.hashCode == itemHashCode;
                    return isFound;
                }
            } finally {
                searchResult.unlock();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            sentinelHead.lock();
            return  sentinelHead.next == null;
        } finally {
            sentinelHead.unlock();
        }
    }

    // searchResult is validated if 1) predecessor is reachable from head,
    // and 2) predecessor points to current
    // For example, X refers to node 4 that was deleted
    //  1 -> 2 -> 3 -> X -> 5
    //  1 -> 2 -> 3 -> X
    private boolean isValidated(SearchResult<E> searchResult) {
        // Validated if set is empty
        if (sentinelHead.next == null) return true;

        // Walk down the list from head until we reach a bigger item or the end of the list
        Node<E> node = sentinelHead;
        while (node != null &&  node.hashCode < searchResult.predecessor.hashCode) {
             node = node.next;
        }

        // Not validated if reached end of list or item was not found
        if (node == null || node.hashCode > searchResult.predecessor.hashCode)
            return false;

        // Last validate check is to ensure predecessor points to current
        return searchResult.predecessor.next == searchResult.current;
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
