package diranieh.distributedCoordination.combining;

import diranieh.utilities.ThreadNumberGenerator;

/**
 * Manages navigation in a binary tree
 */
public class CombiningTree implements ICombiningTree {

    private Node[] leaf;

    public CombiningTree(int size) {
        // Allocate array for the binary tree
        Node[] nodes = new Node[size-1];        // is -1 needed?

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
        int leafArrayLength = (size+1)/2;
        leaf = new Node[leafArrayLength];
        for (int i = 0; i < leaf.length; ++i) {
            leaf[i] = nodes[nodes.length - i  - 1];
        }
    }

    @Override
    public int getAndIncrement() throws InterruptedException {
        // Thread with ids 0 and 1 access the first leaf, threads with ids
        // 2 and 3 access the second leaf and so on. This is why we divide
        // by 2 in the code below
        Node myLeaf = leaf[ThreadNumberGenerator.get() / 2];

        // Start at leaf node and work your way up to the root IF AND ONLY IF
        // this thread is the first to arrive at the node (see precombine impl)
        Node node = myLeaf;
        while (node.precombine())
            node = node.parent;

        // Remember where we stopped. This is either the root, or the last node
        // at which the thread arrived the second
        Node stop = node;


        return 0;
    }
}
