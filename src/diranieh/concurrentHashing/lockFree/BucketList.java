package diranieh.concurrentHashing.lockFree;

import java.util.concurrent.atomic.AtomicMarkableReference;

import static java.lang.Integer.reverse;

public class BucketList<E> {
    static final int MSB_ON_MASK  = 0X80000000;
    static final int LOWEST_3_BYTES_MASK = 0X00FFFFFF;
    Node<E> head;

    private static class Node<E> {
        int key;
        E value;
        AtomicMarkableReference<Node<E>> next;

        // Normal constructor for all nodes except the sentinel
        Node(int key, E value) {
            this.key = key;
            this.value = value;
            this.next = new AtomicMarkableReference<>(null, false);
        }

        // Sentinel constructor
        Node(int key) {
            this.key = key;
            this.value = null;
            this.next = new AtomicMarkableReference<>(null, false);
        }

        private Node<E> getNext() {
            return null;    // TODO
        }
    }

    private static class SearchResult<E> {
        private Node<E> predecessor;
        private Node<E> current;

        public SearchResult(Node<E> predecessor, Node<E> current) {
            this.predecessor = predecessor;
            this.current = current;
        }
    }

    public BucketList() {
        // Create an empty list
        head = new Node<>(0);     // Create a sentinel node to bucket 0

        // Tail is sentinel node to larget possible bucket index
        head.next = new AtomicMarkableReference<Node<E>>(new Node<E>(Integer.MAX_VALUE), false);
    }

    public boolean contains(E item) {
        int key = makeOrdinaryKey(item);
        SearchResult<E> searchResult = find(key);
        return (searchResult.current.key == key);
    }

    private SearchResult<E> find(int key) {
        Node<E> predecessor = head;
        Node<E> current = head.getNext();
        while (current.key < key) {
            predecessor = current;
            current = current.getNext();
        }

        return new SearchResult<E>(predecessor, current);
    }
    // Creating keys for ordinary and sentinel nodex
    private int makeOrdinaryKey(E item) {
        // Take lowest three bytes so as to always have a positive value
        int lowestThreeBytes = item.hashCode() & LOWEST_3_BYTES_MASK;
        int lowestThreeBytesWithMSBOn = lowestThreeBytes | MSB_ON_MASK;
        return reverse(lowestThreeBytesWithMSBOn);
    }

    private int makeSentinelKey(E item) {
        // Take lowest three bytes so as to always have a positive value
        int lowestThreeBytes = item.hashCode() & LOWEST_3_BYTES_MASK;
        return reverse(lowestThreeBytes);
    }
}
