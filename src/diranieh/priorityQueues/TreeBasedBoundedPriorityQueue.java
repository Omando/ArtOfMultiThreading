package diranieh.priorityQueues;

import diranieh.concurrentStacks.ConcurrentLockFreeStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

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
        _root = buildTree_BreadthTraversal(treeHeight);
        Node<E> dummyRoot = buildTree_Recursive(treeHeight, 0); // dummyRoot should be equivalent to _root
    }

    // Builds a binary tree using depth traversal
    private Node<E> buildTree_BreadthTraversal(int treeHeight) {
        Queue<Node<E>> queue = new LinkedList<>();
        Node<E> root = new Node<E>();
        queue.add(root);

        List<Node<E>> nextLevelNodes = new ArrayList<>();
        for (int i = 1; i < treeHeight; i++) {
            while (!queue.isEmpty()) {
                Node<E> node = queue.remove();
                node._left = new Node<>();
                node._right = new Node<>();

                nextLevelNodes.add(node._left);
                nextLevelNodes.add(node._right);
            }

            // Only do for internal nodes
            if (i < treeHeight - 1) {
                for (Node<E> nextLevelNode : nextLevelNodes) {
                    queue.add(nextLevelNode);
                }
                nextLevelNodes.clear();
            }
        }

        // Setup leaves collection
        for (int i = 0; i < nextLevelNodes.size() ; i++) {
            _leaves.set(i, nextLevelNodes.get(i));
        }

        return root;
    }

    // Builds a binary tree using recursion
    private Node<E> buildTree_Recursive( int treeHeight, int poolSlot) {
        Node<E> node = new Node<>();

        // Exit condition - height of a leaf node is zero
        if (treeHeight == 0) {
            node._bin = new ConcurrentLockFreeStack<>();
            _leaves.set(poolSlot, node);
        }

        node._left = buildTree_Recursive( treeHeight - 1, 2 * poolSlot);
        node._right = buildTree_Recursive( treeHeight - 1, (2 * poolSlot) + 1);
        node._left._parent = node._right._parent = node;

        return node;
    }


    @Override
    public void add(E item, int priority) {
        throw new IllegalStateException();
    }

    @Override
    public E removeMin() {
        throw new IllegalStateException();
    }
}
