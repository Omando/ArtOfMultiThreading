package diranieh.concurrentHashing.lockFree;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class BucketList<E> {
    static final int HI_MASK = 0X00800000;
    static final int LOW_MASK = 0X00FFFFFF;

    private static class Node<E> {
        int key;
        E value;
        AtomicMarkableReference<Node> next;

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
}
