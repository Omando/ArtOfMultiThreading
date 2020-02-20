package diranieh.distributedCoordination.combining;

/**
 * Manages navigation in a binary tree
 */
public class CombiningTree {

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
        // Create an array to hold all leaf nodes.
        // Thread i will be assigned to leaf i/2 (for example, threads 0 & 1 goto leaf
        // node 0, threads 2 & 3 goto leaf node 1, etc)
        leaf = new Node[ (size+1)/2];
        for (int i = 0; i < leaf.length; ++i) {
            leaf[i] = nodes[nodes.length - i  - 1];
        }
    }
}
