package diranieh.linkedlistLocking;

import diranieh.utilities.Set;

/**
 * A linked list implementation of an ordered set of keys. The keys are assumed to
 * have minimum and maximum values.
 *
 *  Nodes are sorted in hashcode order, providing an efficient way to detect when an item
 *  is absent.
 *
 *  This class uses a sentinel for the head. Compare the code in this class to
 *  SimpleLinkedListSet2 which does not use a sentinel for the head. The code in this class
 *  is much more compact than that in SimpleLinkedListSet<E>
 *
 * @param <E> the type of elements in this list
 */
public class SimplerLinkedListSet<E> implements Set<E> {

    private static class Node<E> {
        private final E item;
        private final int hashCode;
        private Node<E> next;

        public Node(E item, int hashCode) {
            this(item, hashCode, null);
        }

        public Node(E item, int hashCode, Node<E> next) {
            this.item = item;
            this.hashCode = hashCode;
            this.next = next;
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

    public SimplerLinkedListSet() {
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

        // Start from the head
        SearchResult<E> searchResult = search(itemHashCode);

        // If found, nothing to do
        if (searchResult.current != null && searchResult.current.hashCode == itemHashCode)
            return false;
        else {
            Node<E> newNode = new Node<>(item, itemHashCode);
            searchResult.predecessor.next = newNode;
            newNode.next = searchResult.current;
            return true;
        }
    }

    @Override
    public boolean remove(E item) {
        int itemHashCode = item.hashCode();

        // Start from the head
        SearchResult<E> searchResult = search(itemHashCode);

        // If item is found delete it, otherwise, nothing to do
        if (searchResult.current != null && searchResult.current.hashCode == itemHashCode) {
            searchResult.predecessor.next = searchResult.current.next;
            return true;
        }

        return false;
    }

    @Override
    public boolean contains(E item) {
        int itemHashCode = item.hashCode();
        SearchResult<E> searchResult = search(itemHashCode);
        return  searchResult.current != null &&
                searchResult.current.hashCode == itemHashCode;
    }

    public boolean isEmpty() {
        return sentinelHead.next == null;
    }

    /* Implementation details*/
    private SearchResult<E> search(int itemHashCode) {
        // Start from the head
        Node<E> predecessor = sentinelHead;
        Node<E> current = sentinelHead.next;

        // Search for item
        while (current != null && current.hashCode < itemHashCode) {
            predecessor = current;
            current = current.next;
        }

        return new SearchResult<>(predecessor, current);
    }
}