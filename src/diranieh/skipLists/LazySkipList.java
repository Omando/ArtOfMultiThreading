package diranieh.skipLists;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LazySkipList<E> {
    // Define structure of each node
    private static final class Node<E> {
        private final Lock lock = new ReentrantLock();
        final E item;
        final int key;
        final Node<E>[] next;       // an array!
        volatile boolean marked = false;
        volatile boolean fullyLinked = false;
        private int topLevel;

        // Sentinel node constructor
        public Node(int key) {
            this.key = key;
            item = null;
            next = (Node<E>[])new Node[MAX_LEVEL+1];
            topLevel = MAX_LEVEL;
        }

        public Node(E item, int height) {
            key = item.hashCode();
            this.item = item;
            next = (Node<E>[])new Node[height];
            topLevel = height;
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }
    }

    private static final int MAX_LEVEL = 10;

    // Sentinel nodes
    final Node<E> head = new Node<>(Integer.MIN_VALUE);
    final Node<E> tail = new Node<>(Integer.MAX_VALUE);

    public LazySkipList() {
        // Create an empty skip list: All head.next references point to tail
        // For a linked list, head.next = tail, but for a skip list, head.next
        // is an array and each array element must point to tail
        for (int i = 0; i < head.next.length; i++)
            head.next[i] = tail;
    }

    // Returns -1 if the item is not found, otherwise returns the level
    // at which the item was found. The find() method returns the preds[] and succs[]
    // arrays as well as the level at which the node with a matching key was found.
    int find(E item, Node<E>[] predecessors, Node<E>[] successors) {
        int key = item.hashCode();
        int lFound = -1;

        // Traverse the SkipList starting at the head and at the highest level
        Node<E> predecessor = head;
        for (int level = MAX_LEVEL; level >= 0; level--) {
            Node<E> current = predecessor.next[level];

            // Move right while key to find is greater than the current node's key
            while (key >= current.key) {
                predecessor = current;
                current = predecessor.next[level];
            }

            // We cannot move right any more. Record the level if we find a node
            // with a matching key
            if (lFound == -1 && key == current.key)
                lFound = level;

            // Record the predecessor and current nodes for this level
            predecessors[level] = predecessor;
            successors[level] = current;

            // Continue to the next lower level starting from the predecessor node
        }
        return lFound;
    }

    // Uses find method determine whether a node with the target key k is
    // already in the list
    boolean add(E item) {
        int key = item.hashCode();

        // Nothing to do if an unmarked node with the same key is found (e.g., key
        // is already in the list)


        return false;
    }

}
