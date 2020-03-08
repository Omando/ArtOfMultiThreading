package diranieh.concurrentHashing.lockFree;

import java.util.concurrent.atomic.AtomicMarkableReference;

import static java.lang.Integer.reverse;

public class BucketList<E> {
    static final int HI_MASK  = 0X80000000;
    static final int MASK = 0X00FFFFFF;
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
    }

    public BucketList() {
        // Create an empty list
        head = new Node<>(0);     // Create a sentinel node to bucket 0

        // Tail is sentinel node to larget possible bucket index
        head.next = new AtomicMarkableReference<Node<E>>(new Node<E>(Integer.MAX_VALUE), false);
    }

    // Creating keys for ordinary and sentinel nodex
    private int makeOrdinaryKey(E item) {
        // Take lowest three bytes so as to always have a positive value
        int lowestThreeBytes = item.hashCode() & MASK;
        int lowestThreeBytesWithMSBOn = lowestThreeBytes | HI_MASK;
        return reverse(lowestThreeBytesWithMSBOn);
    }

    private int makeSentinelKey(E item) {
        // Take lowest three bytes so as to always have a positive value
        int lowestThreeBytes = item.hashCode() & MASK;
        return reverse(lowestThreeBytes);
    }
}
