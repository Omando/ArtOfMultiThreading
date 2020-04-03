package diranieh.skipLists;

import java.util.Random;

/* SkipListSet uses a skip list structure to implement a set
* The bottom linked list L0 stores the elements in sorted order
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

            // If height is 1, then the array should have 2 elements
            next = (Node<E>[])new Node[height+1]; // think of next as a vertical array
        }

        int height() {
            return next.length - 1;     // think of next as a vertical array
        }
    }

    private final static int MAX_LEVELS = 32;
    private final Random random;
    private final Node<E> sentinel;
    private final Node<E>[] predecessors;
    private int height;         // the maximum height of any element
    private int count;          // number of elements stored in the skip list

    public SkipListSet() {
        random = new Random();
        count = 0;
        height = 0;
        sentinel = new Node<E>(null, MAX_LEVELS);
        predecessors = (Node<E>[])new Node[MAX_LEVELS];
    }

    // Each step (right or down) takes a constant time. The expected running time
    // is therefore O(Log n)
    public boolean contains(E item) {
        // Get the predecessor node starting from the top level sentinel
        Node<E> predecessor = findPredecessorNode(item);

        // Did we find the item?
        //return predecessor.next[0] == null? null : predecessor.next[0].item;
        return predecessor.next[0].item == item;
    }

    // Search for item and then splice the item into a few lists Lo...Lk where k is
    // selected using getNewNodeHeight.
    public boolean add(E item) {
        Node<E> predecessor = sentinel;     // Start from the sentinel
        int currentLevel = height;
        int compareResult = 0;

        // Iterate over all levels starting from the maximum available height
        while (currentLevel >= 0) {

            // Move right while the next item is less than the search item
            while (predecessor.next[currentLevel] != null &&
                    (compareResult = predecessor.next[currentLevel].item.compareTo(item)) < 0)
                predecessor = predecessor.next[currentLevel];

            // We cannot move right any more. If item is found, then nothing else to do
            if (predecessor.next[currentLevel] != null && compareResult == 0)
                return false;

            // Item was not found at this level. Store the predecessor and go down one
            // level and repeat until we reach level zero
            predecessors[currentLevel] = predecessor;
            currentLevel--;
        }

        // Item not found. We are now at level 0. Create the new node with some random height
        int newNodeHeight = getNewNodeHeight();
        Node<E> newNode = new Node<>(item, newNodeHeight);

        // If new item's height is > the current maximum height, the add additional
        // entries that point to the sentinel (see figure in document for adding 3.5)
        while (height < newNode.height())
            predecessors[++height] = sentinel;      // Increase height

        // see DSALG_LinkedList document -> adding 3.5, page 8 & 9
        for (int i = 0; i < newNode.next.length; i++) {
            newNode.next[i] = predecessors[i].next[i];      // adding 3.5, top figure
            predecessors[i].next[i] = newNode.next[i];      // adding 3.5, bottom figure
        }

        count++;        // increment the number of elements in the list
        return true;
    }

    public boolean remove(E item) {
        Node<E> predecessor = sentinel;     // Start from the sentinel
        int currentLevel = height;
        int compareResult = 0;
        boolean nodeRemoved = false;

        // Iterate over all levels starting from the maximum available height
        while (currentLevel >= 0) {

            // Move right while the next item is less than the search item
            while (predecessor.next[currentLevel] != null &&
                    (compareResult = predecessor.next[currentLevel].item.compareTo(item)) < 0)
                predecessor = predecessor.next[currentLevel];

            // We cannot move right any more. If item is found, remove it
            if (predecessor.next[currentLevel] != null && compareResult == 0) {
                nodeRemoved = true;
                predecessor.next[currentLevel] = predecessor.next[currentLevel].next[currentLevel];
                if (predecessor == sentinel && predecessor.next[currentLevel] == null)
                    height--;       // See diagram on page 9 in DSALG_LinkedLists.docx
            }

            // Go down one level and repeat until we reach level zero
            currentLevel--;
        }

        if (nodeRemoved) count--;        // increment the number of elements in the list
        return nodeRemoved;
    }

    public int count() {
        return count;
    }

    // Simulate tossing a coin: given a 32-bit random integer, a bit with value 1 represents
    // heads and a bit with value 0 represents tails. To simulate getting a tails from tossing
    // a coin, get a 32-bit random integer and count the number of trailing 1s (heads) in the
    // number's binary representation. The first 0 in the binary representation of the random
    // number represents a tails, and the location of that first 0 is the height.
    // Note Maximum height returned is 32 since the random number is a 32-bit integer
    private int getNewNodeHeight() {
        int height = 0;     // Initial height is 0
        int nthBit = 1;     // Start at bit 1
        int randomNumber = random.nextInt();

        // Repeat while the nth bit of randomNumber is 1 (heads)
        while ((randomNumber & nthBit) != 0) {
            height++;           // Increment height
            nthBit <<= 1;       // Left shift m by one 1 to move to the next more significant bit
        }

        // The nth bit of randomNumber is 0 (tails). The location of the nth bit
        // is the required height
        return height;
    }

    private Node<E> findPredecessorNode(E item) {
        Node<E> predecessor = sentinel;
        int currentLevel = height;

        // Loop until we reach Level 0
        while (currentLevel >= 0) {

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
