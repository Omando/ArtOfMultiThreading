package diranieh.distributedCoordination.combining;

/**
 * Manages each visit to the node by performing the following actions:
 *  Synchronize access
 *  Combine state
 *  Bookkeeping
 */
public class Node {
    // Different states of the node
    private enum State {IDLE, FIRST, SECOND, RESULT, ROOT}
    private final Object locker = new Object();
    private boolean locked;
    private State state;
    private int firstValue;     // value to combine
    private int secondValue;    // value to combine
    private int result;         // result of combining
    Node parent;

    // Constructor for root node. Combining state is always ROOT
    public Node() {
        state = State.ROOT;
        locked = false;
    }

    // Constructor for all other nodes. Initial combining state is IDLE
    public Node(Node parent) {
        state = State.IDLE;
        locked = false;
        this.parent = parent;
    }

    /* Return a boolean to indicate whether the thread was the FIRST to arrive
    at the current node*/
    public boolean precombine() throws InterruptedException {
        synchronized (locker) {

            // Wait while the node is locked
            while (locked)
                wait();

            // Check combining status
            switch (state) {
                // Thread will return to check for a value to combine with
                case IDLE:
                    state = State.FIRST;
                    return true;        // continue up the tree
                // An earlier thread has previously visited this node.
                case FIRST:
                    // First thread is moving up, so lock the tree to prevent the earlier
                    // visiting thread from proceeding without combining with this thread’s
                    // value.
                    locked = true;
                    state = State.SECOND;   // Prepare to deposit 2nd value
                    return false;           // End of phase 1. Do not move up the tree
                // Can't continue up the tree if this is the root
                case ROOT:
                    return false;           // End of phase 1. Do not move up the tree
                // Programming defensively ... check for illegal states
                default:
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }
}
