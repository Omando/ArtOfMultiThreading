package diranieh.linkedlistLocking;

import diranieh.utilities.Set;

import java.util.concurrent.locks.ReentrantLock;

/**
 * LazyConcurrentSet refines  {@link OptimisticConcurrentSet} such that the
 * contains method is wait-free and add/remove methods, while still blocking,
 * traverse the list only once.
 *
 * The key insight is that deleting nodes causes troubles, so we’d like to do
 * it lazily. This is done by adding a marked boolean field to the Node class;
 * a marked node (marked = true) means that the node is unreachable. This is
 * called logical removal. Physical removal is when predecessor.next is redirected
 * to node.next.
 *
 * Adding marked field eliminates the need to validate that the node is reachable
 * by re-traversing the whole list. If a thread finds a marked node, then that
 * node is not in the set.
 * @param <E>
 */
public class LazyConcurrentSet<E> implements Set<E> {
    private static class Node<E> {
        private final E item;

        // The hashCode field is the item’s hash code. Nodes are sorted in hashcode order,
        // providing an efficient way to detect when an item is absent.
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
                        searchResult.current.isMarked = true;   // NODE IS DELETED!!
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

    // A wait-free implementation that returns true if the node of interest
    // is present and unmarked
    @Override
    public boolean contains(E item) {
        int itemHashCode = item.hashCode();

        // Start at the head
        Node<E> current = sentinelHead.next;

        // Search for key
        while (current != null &&  itemHashCode > current.hashCode)
            current = current.next;                 // Traverse without locking (nodes may have been removed)

        return  current != null &&                  // linked list is not empty or have not reached the end
                !current.isMarked &&                // current node is not logically deleted
                current.hashCode == itemHashCode;   // current node is equal to item of interest
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

    // predecessor and current nodes are validated if both are NOT marked,
    // and predecessor points to current
    private boolean isValidated(Node<E> predecessor, Node<E> current) {
        return !predecessor.isMarked &&                     // is predecessor not deleted?
                (current == null || !current.isMarked) &&   // is current not deleted?
                predecessor.next == current;                // does predecessor point to current?
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
