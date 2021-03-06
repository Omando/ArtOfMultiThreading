package diranieh.priorityQueues;

import diranieh.concurrentStacks.ConcurrentLockFreeStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This tree based bounded priority queue is implemented as a binary tree
 * of TreeNode objects.
 *
 * If the priority range is 0 to m-1, then the tree has m leaves.
 *
 * Nodes are used as follows:
 * The ith leaf node has a container (ConcurrentLockFreeStack<E>) that holds
 * items of priority i. Internal nodes hold the number of items in the subtree
 * rooted at the node’s left child. Because each internal node holds a counter,
 * there are m-1 shared bounded counters in the tree’s internal nodes.
 *
 * @param <E> the type of the elements in the priority queue
 */
public class TreeBasedBoundedPriorityQueue<E> implements PriorityQueue<E> {
    private static class Node<E> {
        private final AtomicInteger _counter;
        private ConcurrentLockFreeStack<E> _bin;
        private Node<E> _left;
        private Node<E> _right;
        private Node<E> _parent;

        public Node() {
            _counter = new AtomicInteger(0);
        }

        // A leaf node has no children
        private boolean isLeaf() { return _left == null && _right == null;}
    }

    private Node<E> _root;
    private List<Node<E>> _leaves;  //  Easy access to leaves. No need to search for leaf from the root

    // In a binary tree, tree depth or height determines the number of leaf nodes as follows
    public TreeBasedBoundedPriorityQueue(int treeHeight) {
        int range = 1 << treeHeight;      // if logRange = 4, then range is 2^4 = 16 or 10000
        _leaves = new ArrayList<>(range);

        _root = buildTree_BreadthTraversal(treeHeight); // Build the priority queue binary tree
        //_root = buildTree_Recursive(treeHeight, 0);   // A recursive version of build the pq binary tree.
    }

    // Builds a binary tree using depth traversal
    private Node<E> buildTree_BreadthTraversal(int treeHeight) {
        Queue<Node<E>> queue = new LinkedList<>();
        Node<E> root = new Node<E>();
        queue.add(root);

        List<Node<E>> nextLevelNodes = new ArrayList<>();
        for (int i = 1; i <= treeHeight; i++) {
            while (!queue.isEmpty()) {
                Node<E> node = queue.remove();
                node._left = new Node<>();
                node._right = new Node<>();
                node._left._parent = node._right._parent = node;

                nextLevelNodes.add(node._left);
                nextLevelNodes.add(node._right);
            }

            // Only do for internal (non-leaf) nodes
            if (i <= treeHeight - 1) {
                for (Node<E> nextLevelNode : nextLevelNodes) {
                    queue.add(nextLevelNode);
                }
                nextLevelNodes.clear();
            }
        }

        // Setup leaves collection
        for (int i = 0; i < nextLevelNodes.size() ; i++) {
            Node<E> node = nextLevelNodes.get(i);
            node._bin = new ConcurrentLockFreeStack<>();
            _leaves.add(i, node);
        }

        return root;
    }

    // Builds a binary tree using recursion
    private Node<E> buildTree_Recursive( int treeHeight, int poolSlot) {
        Node<E> node = new Node<>();

        // Exit condition - height of a leaf node is zero
        if (treeHeight == 0) {
            node._bin = new ConcurrentLockFreeStack<>();
            _leaves.add(poolSlot, node);
            return node;
        }

        // (2 * poolSlot) and (2 * poolSlot) + 1: When a binary tree is implemented as
        // an array, node at index k has children at index 2k and 2k+1
        node._left = buildTree_Recursive( treeHeight - 1, 2 * poolSlot);
        node._right = buildTree_Recursive( treeHeight - 1, (2 * poolSlot) + 1);
        node._left._parent = node._right._parent = node;

        return node;
    }


    @Override
    public void add(E item, int priority) {
        // Get leaf node for this priority
        Node<E> node = _leaves.get(priority);

        // Add this item to the leave's bin collection
        node._bin.push(item);

        // Traverse up to the root, incrementing the counter if ascending from left
        while(node != _root) {
            Node<E> parent = node._parent;
            if (node == parent._left) { // increment if ascending from left
                parent._counter.getAndIncrement();
            }
            node = parent;
        }
    }

    @Override
    public E removeMin() {
        Node<E> node = _root;
        while(!node.isLeaf()) {
            if (node._counter.getAndDecrement() > 0 ) {
                node = node._left;
            } else {
                node = node._right;
            }
        }
        return node._bin.pop(); // if null pqueue is empty
    }
}
