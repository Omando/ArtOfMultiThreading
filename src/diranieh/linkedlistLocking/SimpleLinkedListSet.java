package diranieh.linkedlistLocking;

/** Linked list implementation of a set
 Nodes are sorted in hashcode order, providing an efficient way to detect when an item
 is absent.
 */
public class SimpleLinkedListSet<T> implements Set<T> {
    private static class Node<T> {
        T item;
        int hashCode;
        Node<T> next;

        public Node(T item) {
            this(item, null);
        }
        public Node(T item, Node<T> next) {
            this.item = item;
            this.hashCode = item.hashCode();
            this.next = next;
        }
    }

    // Holds the result of calling SimpleSet<T>.search
    private static class SearchResult<T> {
        Node<T> predecessor;
        Node<T> current;

        public SearchResult(Node<T> predecessor, Node<T> current) {
            this.predecessor = predecessor;
            this.current = current;
        }
    }

    // Sentinels
    private Node<T> head;
    private Node<T> tail;

    /* Adds an item to the set according to the sort order imposed by the key
    * Iterate the list comparing keys. If a match is found, the item is already present
    * and false is returned. If a node with a higher key is encountered, the item is not
    * in the set. The item is added before the node with the higher key, and true is
    * returned*/
    @Override
    public boolean add(T item) {
        Node<T> newNode = new Node<>(item);

        // new item becomes head and tail if set is empty
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            return true;
        }

        // Prepend item if less than head
        if (newNode.hashCode < head.hashCode) {
            newNode.next = head;
            head = newNode;
            return true;
        }

        // Append item if greater than tail
        if (newNode.hashCode > tail.hashCode) {
            tail.next = newNode;
            tail = newNode;
            return true;
        }

        //search for item  between head and tail
        SearchResult<T> searchResult = search(newNode.hashCode);

        // Return false if found between head and tail, otherwise insert between
        // predecessor and current nodes
        if (searchResult.current.hashCode == newNode.hashCode) {
            return false;
        } else {
            searchResult.predecessor.next = newNode;
            newNode.next = searchResult.current;
            return true;
        }
    }

    @Override
    public boolean remove(T item) {
        // Nothing to do if list is empty, or item's hash code is outside the range
        // identified by the hash codes of head and tail
        int hashCode = item.hashCode();
        if (isEmpty() || hashCode < head.hashCode || hashCode > tail.hashCode) {
            return false;
        }

        //search for item  between head and tail
        SearchResult<T> searchResult = search(hashCode);

        // Remove node if found, otherwise nothing to do and return false
        if (searchResult.current.hashCode == hashCode) {
            if (searchResult.current == head && searchResult.current == tail) {
                // We are removing the last item
                head = null;
                tail = null;
            }
            else if (searchResult.current == head) {
                // Adjust head to point to the next item if given item refers to head
                head = head.next;
            } else if (searchResult.current == tail) {
                // Adjust tail to point to the predecessor item if given item refers to tail
                searchResult.predecessor.next = null;
                tail = searchResult.predecessor;
            } else {
                // Item is somewhere between tail and head
                searchResult.predecessor.next = searchResult.current.next;
            }
            return true;
        } else {
            // Not found, so nothing to do
            return false;
        }
    }

    @Override
    public boolean contains(T item) {
        int hashCode = item.hashCode();
        if (isEmpty() || hashCode < head.hashCode || hashCode > tail.hashCode) {
            return false;
        }

        SearchResult<T> searchResult = search(hashCode);
        return searchResult.current.hashCode == hashCode;
    }

    public boolean isEmpty() {
        return head == null && tail == null;
    }

    /* Implementation details*/
    private SearchResult<T> search(int itemHashCode) {
        // Check if item's hashcode is outside the range of head and tail
        if (itemHashCode < head.hashCode || itemHashCode > tail.hashCode) {
            return new SearchResult<>(null, null);
        }

        // Item's hash code is somewhere between head and tail
        Node<T> current = head;
        Node<T> predecessor = null;
        while (current.hashCode < itemHashCode) {
            predecessor = current;
            current = current.next;
        }

        return new SearchResult<>(predecessor, current);
    }
}
