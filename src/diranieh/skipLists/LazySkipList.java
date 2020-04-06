package diranieh.skipLists;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* A concurrent implementation of a skip list based on LazyConcurrentSet

 This highest level can be maintained dynamically to reflect the highest level
 actually in the SkipList. This was done in SkipListSet, but for brevity, we do
 not do so here and searches always start at the max level
* */
public class LazySkipList<E> {
    // Define structure of each node
    private static final class Node<E> {
        /* item, hashCode and next field are the core fields of a Node class */
        private final E item;

        // The hashCode field is the item’s hash code. Nodes are sorted in hashcode order,
        // providing an efficient way to detect when an item is absent.
        private final int hashCode;
        private final Node<E>[] next;       // an array!
        private int topLevel;

        /* locked, marked and fullyLinked fields are used for concurrency control*/
        private final Lock lock = new ReentrantLock();
        private volatile boolean marked = false;        // true if the node was logically deleted, else false
        private volatile boolean fullyLinked = false;   // true if the node was linked in lists at all levels.

        // Sentinel node constructor
        public Node(int hashCode) {
            this.hashCode = hashCode;
            item = null;
            next = (Node<E>[])new Node[MAX_LEVEL+1];
            topLevel = MAX_LEVEL;
        }

        public Node(E item, int height) {
            hashCode = item.hashCode();
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

    private static final int MAX_LEVEL = 32;
    private final Node<E> head = new Node<>(Integer.MIN_VALUE);     // Sentinel node
    private final Node<E> tail = new Node<>(Integer.MAX_VALUE);     // Sentinel node
    private final Random random;

    public LazySkipList() {
        // Create an empty skip list: All head.next references point to tail
        // For a linked list, head.next = tail, but for a skip list, head.next
        // is an array and each array element must point to tail
        for (int i = 0; i < head.next.length; i++)
            head.next[i] = tail;

        random = new Random();
    }

    // Uses find method determine whether a node with the target hash code is
    // already in the list. If an unmarked node with the hashcode is found, we
    // just return false (cannot add an existing item as a set is mathematically
    // unique). However, if that node is not yet fully linked (indicated by the
    // fullyLinked field), then the thread waits until it it linked, because the
    // key (hashcode) is not in the abstract set until the node is fully linked
    boolean add(E item) {
        int topLevel = getNewNodeHeight();
        Node<E>[] preds = (Node<E>[]) new Node[MAX_LEVEL + 1];
        Node<E>[] succs = (Node<E>[]) new Node[MAX_LEVEL + 1];

        while (true) {
            // Try to find the given item
            int foundAtLevel = find(item, preds, succs);
            if (foundAtLevel != -1) {
                // Item found. Get the actual node
                Node<E> nodeFound = preds[foundAtLevel];

                // Given item found, but the item is in the set if that node is both unmarked
                // AND fully linked. It is safe to check if the node is unmarked before the
                // node is fully linked, because remove() methods do not mark nodes unless
                // they are fully linked
                if (!nodeFound.marked) {
                    // Found item was not marked for deletion. If it's not fully linked, wait
                    // for it to be fully linked before returning false (item is in list if
                    // and only if it is both marked AND fully linked
                    while (!nodeFound.fullyLinked) {/* Empty */}
                    return false;       // item marked and fully linked. We can return false
                }

                // The node found is actually marked;  some other thread is in the process of
                // deleting it, so the add() call simply retries.
                continue;
            }

            // Item not found in the list. preds and succs references are unreliable, because
            // they may no longer be accurate by the time the nodes are accessed. Proceed to
            // lock and validate each of the predecessors returned by find() from level 0 up
            // to the topLevel of the new node
            int highestLocked = -1;
            try {
                Node<E> pred, succ;
                boolean valid = true;
                for (int level = 0; valid && (level <= topLevel); level++) {
                    pred = preds[level];
                    succ = succs[level];
                    pred.lock.lock();
                    highestLocked = level;

                    // Check that the predecessor is still adjacent to the successor and that neither
                    // is marked.
                    valid = !pred.marked && !succ.marked && pred.next[level] == succ;
                }

                // If validation fails, release the locks an retry again (see finally block)
                if (!valid) continue;

                // If the thread successfully locks and validates the results of find up to the
                // topLevel of the new node, then the add() call will succeed because the thread
                // holds all the locks it needs. The thread then allocates a new node with the
                // appropriate key and randomly chosen topLevel, links it in, and sets the new
                // node’s fullyLinked flag. It then releases all its locks and returns true.
                Node<E> newNode = new Node(item, topLevel);
                for (int level = 0; level <= topLevel; level++)
                    newNode.next[level] = succs[level];
                for (int level = 0; level <= topLevel; level++)
                    preds[level].next[level] = newNode;
                newNode.fullyLinked = true; // successful add linearization point
                return true;

            } finally {
                // If validation fails, the thread must have encountered the effects of a
                // conflicting method, so it releases the locks it acquired and retries.
                for (int level = 0; level <= highestLocked; level++)
                    preds[level].unlock();
            }
        }
    }

    boolean remove(E item) {
        Node<E> nodeToRemove = null; boolean isMarked = false; int topLevel = -1;
        Node<E>[] preds = (Node<E>[]) new Node[MAX_LEVEL + 1];
        Node<E>[] succs = (Node<E>[]) new Node[MAX_LEVEL + 1];

        // Keep on trying until we succeed
        while (true) {
            // Try to find the given item
            int foundAtLevel = find(item, preds, succs);

            // We found a level where a node with the same item exists. Get the node
            // from that level
            if (foundAtLevel != -1) nodeToRemove = succs[foundAtLevel];

            // Is the node ready to be deleted? A node is ready to be deleted if  it is
            // fully linked, unmarked, and at its top level. A node found below its top
            // level was either not yet fully linked or marked and already partially
            // unlinked by a concurrent remove() method call
            if (isMarked || (foundAtLevel != -1 &&
                            nodeToRemove.fullyLinked &&
                            nodeToRemove.topLevel == foundAtLevel &&
                            !nodeToRemove.marked)) {

                // Verify that the node is still not marked. If it is still not marked,
                // the thread locks the node and marks it (indicating the node is
                // logically deleted)
                if (!isMarked) {
                    topLevel = nodeToRemove.topLevel;
                    nodeToRemove.lock.lock();

                    // If the node was marked, then the thread returns false since the node
                    // was already deleted.
                    if (nodeToRemove.marked) {
                        nodeToRemove.lock.unlock();
                        return false;
                    }
                    nodeToRemove.marked = true;
                    isMarked = true;
                }

                // The rest of the code completes the physical deletion of the nodeToRemove node
                int highestLocked = -1;
                try {
                    // To remove nodeToRemove from the list, first lock (in ascending order,
                    // to avoid deadlock) the nodeToRemove’s predecessors at all levels up to
                    // the nodeToRemove’s topLevel
                    Node<E> pred, succ; boolean valid = true;
                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds[level];
                        pred.lock.lock();
                        highestLocked = level;

                        // Validate that the predecessor is still unmarked and still refers
                        // to nodetoRemove.
                        valid = !pred.marked && pred.next[level] == nodeToRemove;
                    }
                    if (!valid) continue;

                    // Splice out nodetoRemove one level at a time. To maintain the SkipList
                    // property, that any node reachable at a given level is reachable at
                    // lower levels, the victim is spliced out from top to bottom.
                    for (int level = topLevel; level >= 0; level--) {
                        preds[level].next[level] = nodeToRemove.next[level];
                    }

                    // After successfully removing the node, the thread releases all its locks
                    // and returns true
                    nodeToRemove.lock.unlock();
                    return true;
                } finally {
                    for (int i = 0; i <= highestLocked; i++) {
                        preds[i].unlock();
                    }
                }
            } else return false;
        }
    }

    // The contains method is usually the most common and is wait-free
    boolean contains(E item) {
        Node<E>[] preds = (Node<E>[]) new Node[MAX_LEVEL + 1];
        Node<E>[] succs = (Node<E>[]) new Node[MAX_LEVEL + 1];

        // Locate the node
        int foundAtLevel = find(item, preds, succs);

        // A node is found if it is fully linked and unmarked for deletion
        return (foundAtLevel != -1
                && succs[foundAtLevel].fullyLinked
                && !succs[foundAtLevel].marked);
    }

    // Returns -1 if the item is not found, otherwise returns the level at which
    // the item was found. The find() method returns the preds[] and succs[]
    // arrays as well as the level at which the node with a matching key was found.
    private int find(E item, Node<E>[] predecessors, Node<E>[] successors) {
        int hashCode = item.hashCode();
        int lFound = -1;

        // Traverse the SkipList starting at the head and at the highest level
        Node<E> predecessor = head;
        for (int level = MAX_LEVEL; level >= 0; level--) {
            Node<E> current = predecessor.next[level];

            // Move right while key to find is greater than the current node's key
            while (hashCode >= current.hashCode) {
                predecessor = current;
                current = predecessor.next[level];
            }

            // We cannot move right any more. Record the level if we find a node
            // with a matching key
            if (lFound == -1 && hashCode == current.hashCode)
                lFound = level;

            // Record the predecessor and current nodes for this level
            predecessors[level] = predecessor;
            successors[level] = current;

            // Continue to the next lower level starting from the predecessor node
        }
        return lFound;
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
}
