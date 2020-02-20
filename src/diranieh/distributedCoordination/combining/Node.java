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
    private boolean locked;
    private State state;
    private int firstValue;     // value to combine
    private int secondValue;    // value to combine
    private int result;         // result of combining
    private Node parent;

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
}
