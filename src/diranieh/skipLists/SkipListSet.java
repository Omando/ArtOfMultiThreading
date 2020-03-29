package diranieh.skipLists;

import java.util.Random;

/* SkipListSet uses a skip list structure to implement a set
* The most bottom linkd list L0 stores the elements in sorted order
* */
public class SkipListSet<E extends Comparable<E>> {

    /* A space-efficient way to implement a skip list is to define a Node,
    as consisting of a data value 'item', and an array of pointers 'next'
    where next[i] points to the node's successor in the list. In this way,
    the data value in a node is referenced only once, even though the data
    value may appear in several lists */
    private static class Node<E> {
        final E item;
        final Node<E>[] next;

        public Node(E item, int height) {
            this.item = item;
            next = (Node<E>[])new Node[height]; // think of next as a vertical array
        }

        int height() {
            return next.length - 1;     // think of next as a vertical array
        }
    }

    private final static int MAX_LEVELS = 32;
    private Node<E> sentinel;
    private int height;         // the maximum height of any element
    private int count;          // number of elements stored in tje skip list
    private Random random;

    public SkipListSet() {
        random = new Random();
        count = 0;
        height = 0;
        sentinel = new Node<E>(null, MAX_LEVELS);
    }

    // Each step (right or down) takes a constant time. The expected running time
    // is therefore O(Log n)
    public E find(E item) {
        // Get the predecessor node starting from the top level sentinel
        Node<E> predecessor = findPredecessorNode(item);

        // Did we find the item?
        return predecessor.next[0] == null? null : predecessor.next[0].item;
    }

    public boolean add(E item) {
        // to be done....
        return false;
    }

    // Simulate tossing a coin: given a 32-bit random integer, a bit with value 1 represents
    // heads and a bit with value 0 represents tails. To simulate getting a tails from tossing
    // a coin, get a 32-bit random integer and count the number of trailing 1s (heads) in the
    // number's binary representation. The first 0 in the binary representation of the random
    // number represents a tails, and the location of that first 0 is the height.
    // Note Maximum height returned is 32 since the random number is a 32-bit integer
    private int getNewNodeHeight() {
        int z = random.nextInt();
        int height = 0;
        int m = 1;      // bit 1

        // Repeat while bit m of number z is 1 (heads)
        while ((z & m) != 0) {
            height++;       // Increment height
            m <<= 1;        // Left shift m by one 1
        }

        // Bit m in z is 0 (tails). Its location is the required height
        return height;
    }

    private Node<E> findPredecessorNode(E item) {
        Node<E> predecessor = sentinel;
        int currentLevel = height;

        // Loop until we reach Level 0
        while (currentLevel <= 0) {

            // Move right while the next item is less than the search item
            while (predecessor.next[currentLevel] != null &&
                    predecessor.next[currentLevel].item.compareTo(item) < 0)
                predecessor = predecessor.next[currentLevel];

            // We cannot move right any more. Move one level down
            currentLevel--;
        }

        return predecessor;
    }


}
