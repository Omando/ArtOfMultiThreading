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

            // Check pre-combining status
            switch (state) {
                case IDLE:              // Thread will return to check for a value to combine with
                    state = State.FIRST;
                    return true;        // continue up the tree
                case FIRST:             // An earlier thread has previously visited this node.
                    // The earlier thread is moving up, so lock the tree to prevent the earlier
                    // visiting thread from proceeding without combining with this thread’s
                    // value.
                    locked = true;
                    state = State.SECOND;   // Prepare to deposit 2nd value
                    return false;           // End of phase 1. Do not move up the tree
                case ROOT:                  // Can't continue up the tree if this is the root
                    return false;           // End of phase 1. Do not move up the tree
                default:                    // Programming defensively; check for illegal states
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    public int combine(int combined) throws InterruptedException {
        synchronized (locker) {
            while (locked) wait();      // wait here until node is unlocked

            locked = true;              // Lock out late attempts to combine
            firstValue = combined;      // Remember our contribution
            switch (state) {            // Check combining status
                case FIRST:             // First thread is done
                    return firstValue;
                case SECOND:            // Combine with second thread
                    return firstValue + secondValue;
                default:                // Programming defensively; check for illegal states
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    public int operation(int combined) throws InterruptedException {
        synchronized (locker) {
            switch (state) {
                case ROOT:
                    int oldValue = result;      // cache prior value
                    result += combined;         // add sum to root
                    return oldValue;            // return prior value
                case SECOND:
                    secondValue = combined;     // deposit value for later combining
                    locked = false;             // unlock node and notify 1st thread
                    notifyAll();

                    while (state != State.RESULT)   // wait for 1st thread to deliver result
                        wait();

                    // Unlock node and return
                    locked = false;
                    notifyAll();
                    state = State.IDLE;
                    return result;
                default:                        // Programming defensively; check for illegal states
                    throw new IllegalStateException("Unknown state: " + state);
            }
        }
    }

    public void distribute(int prior) {
        synchronized (locker) {
            switch (state) {
                case FIRST:
                    // No combining, unlock node and reset
                    state = State.IDLE;
                    locked = false;
                    break;
                case SECOND:
                    // Notify second thread that result is available
                    result = prior + firstValue;
                    state = State.RESULT;
                    break;
                default:                        // Programming defensively; check for illegal states
                    throw new IllegalStateException("Unknown state: " + state);
            }
            notifyAll();
        }
    }
}
