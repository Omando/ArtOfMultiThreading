package diranieh.distributedCoordination.combining;

import diranieh.utilities.ThreadNumberGenerator;

import java.util.Stack;

/*
 * Manages navigation in a binary tree
 *
 * The combining tree is a binary tree implemented using an array.
 * Recall these useful facts about binary tree:
 *  If N is the number of nodes, then the number of leaves L = (N+1)/2
 *  If L is the number of leaves, the the number of nodes N = 2L - 1
 */
public class CombiningTree implements ICombiningTree {

    private Node[] leaf;

    /*  Parameter threadCount is the number of threads operating on the combining
    tree (a binary tree). Leaf nodes are the entry point for threads into the
    combining tree, and each leaf node can be accessed by two threads only.

    Parameter threadCount determine how many nodes are required for the tree as
    follows:
        Number of threads = T
        Number of leaves L =  T/2
        Number of nodes = 2L - 1 = T - 1

    For example,
        if T = 8, then we need 4 leaves and number of nodes = 7
        if T = 6, then we need 3 leaves and number of nodes = 5
        if T = 7, then we need 3.5 leaves which is rounded up to 4 and number of nodes = 7
    */
    public CombiningTree(int threadCount) {

        // Determine node and leaf count based on thread count
        int leafCount, nodeCount;
        if (threadCount == 1) {             // You only need the root node for 1 thread
            leafCount = nodeCount = 1;
        } else if (threadCount == 2) {      // You need a leaf node and the root node for two threads
            leafCount = 1;
            nodeCount = 2;
        } else {                            // General case for more than 2 threads
            leafCount = (int) Math.ceil(threadCount / 2.0);   // if T = 7, leaves is rounded up from 3.5 to 4
            nodeCount =  (2 * leafCount) - 1;
        }

        // Allocate array for the binary tree
        Node[] nodes = new Node[nodeCount];

        // Node[0] is the root. Initialize use the root constructor
        nodes[0] = new Node();  // Special constructor for root only

        // Nodes[1..length-1] are child nodes
        for (int i = 1; i < nodes.length; ++i) {
            int parentIndex = (i-1)/2;
            Node parentNode = nodes[parentIndex];
            nodes[i] = new Node(parentNode);
        }

        // The leaf nodes are the last (size+1)/2 nodes in the array
        // Create an array to hold all leaf nodes, where the length of
        // the array = (size + 1) / 2
        // Thread i will be assigned to leaf i/2 (for example, threads 0 & 1 goto leaf
        // node 0, threads 2 & 3 goto leaf node 1, etc)
        leaf = new Node[leafCount];
        for (int i = 0; i < leaf.length; ++i) {
            leaf[i] = nodes[nodes.length - i  - 1];
        }
    }

    @Override
    public int getAndIncrement() throws InterruptedException {
        // Threads with ids 0 and 1 access the first leaf, threads with ids
        // 2 and 3 access the second leaf, and so on. This is why we divide
        // by 2 in the code below
        Node myLeaf = leaf[ThreadNumberGenerator.get() / 2];

        /* PRE-COMBINING PHASE */
        // Start at leaf node and work your way up to the root IF AND ONLY IF
        // this thread is the first to arrive at the node (see precombine impl)
        Node node = myLeaf;
        while (node.precombine())
            node = node.parent;

        // Remember where we stopped. This is either the root, or the last node
        // at which the thread arrived the second
        Node stop = node;

        /* COMBINING PHASE */
        node = myLeaf;          // IMPORTANT to note that the combining phase starts from the leaf
        int combined = 1;       // Increment by 1 (could be passed as a ctor parameter to create a custom counter)
        Stack<Node> stack = new Stack<>();
        while (node != stop) {                      // Revisit nodes visited in phase 1
            combined = node.combine(combined);      // Accumulate combined values, if any
            stack.push(node);                       // Path will be retraversed in reverse order in the next phase
            node = node.parent;                     // Move up the tree
        }

        /* OPERATION PHASE */
        int prior = stop.operation(combined);       // Get result of combining on the stop node

        /* DISTRIBUTION PHASE*/
        while (!stack.empty()) {        // Traverse path in reverse order
            node = stack.pop();
            node.distribute(prior);     // distribute results to waiting second threads
        }
        return prior;                   // return result to caller
    }
}
