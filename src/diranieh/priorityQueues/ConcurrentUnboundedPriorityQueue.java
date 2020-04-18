package diranieh.priorityQueues;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ConcurrentUnboundedPriorityQueue<E> implements PriorityQueue<E> {
    // recall some basics: static means only copy available for all classes.
    // To ensure this single copy is immutable, make it final
    private final static int ROOT_INDEX = 1;
    private final Lock _heapLock;
    private int _next;
    private HeapNode<E>[] _heap;
    private enum HeapNodeStatus {
        NOT_IN_USE,     // Empty, not ready for use
        AVAILABLE,      // Node holds an item and a priority
        BUSY            // Node is being percolated up the tree
    }

    // Each element in the heap array is a HeapNode object that contains an item and
    // its priority, a lock field held for short modifications, a tag field to hold
    // node status, and the id of thread currently holding the lock
    private static class HeapNode<E> {
        private final Lock _lock;
        private  int _priority;
        private  E _item;
        private HeapNodeStatus _tag;
        private long _threadId;

        public HeapNode() {
            _lock = new ReentrantLock();
            _tag = HeapNodeStatus.NOT_IN_USE;
        }

        public void init(E item, int priority) {
            _item = item;
            _priority = priority;
            _tag = HeapNodeStatus.BUSY;
            _threadId = Thread.currentThread().getId();
        }

        private void lock() {_lock.lock();}
        private void unlock() { _lock.unlock();}
        private void resetOwner() { _threadId = -1;}

        // returns true if and only if the node’s tag is BUSY and the
        // node's owner is the current thread.
        private boolean isOwner() {
            if (_tag == HeapNodeStatus.AVAILABLE || _tag == HeapNodeStatus.NOT_IN_USE)
                return false;
            return (_tag == HeapNodeStatus.BUSY && _threadId == Thread.currentThread().getId());
        }
    }

    public ConcurrentUnboundedPriorityQueue(int capacity) {
        _heapLock = new ReentrantLock();

        // We ignore first array entry. This means that a node at index k has left and right
        // children at indices 2k and 2k+1, respectively
        _next = 1;                                           // Ignore first array entry
        _heap = (HeapNode<E>[]) new HeapNode[capacity + 1];  // +1: we are ignoring item at index 0

        for(int i = 1; i < capacity + 1; i++)
            _heap[i] = new HeapNode<>();
    }

    @Override
    public void add(E item, int priority) {
        // Acquire the global lock preventing any other thread from accessing the
        // first empty heap slot that will be used to host the newly added item
        _heapLock.lock();
            int child = _next++;
            _heap[child].lock();
            _heap[child].init(item, priority);
            _heap[child].unlock();

            if (_next >= _heap.length)
                resize();
        _heapLock.unlock();

        // The node for the new item must now swim up the priority queue tree in order
        // to re-balance the tree. Keep on swimming up as long as we have not reached
        // the root
        while (child > ROOT_INDEX) {
            // Calculate the parent index, then swap child and parent if child has a lower priority
            int parent = child / 2;

            // Lock parent and child. All locks are acquired in ascending order
            _heap[parent].lock();
            _heap[child].lock();
            int oldChild = child;
            try {

                // Compare priorities of parent and child if the parent is AVAILABLE and
                // the child is owned by the caller
                if (_heap[parent]._tag == HeapNodeStatus.AVAILABLE && _heap[child].isOwner()) {
                    if (_heap[child]._priority < _heap[parent]._priority) {
                        // Swap parent and child nodes since child has a higher priority
                        swap(child, parent);
                        child = parent;
                    } else {
                        // the node is where it belongs and it is marked AVAILABLE and unowned
                        _heap[child]._tag = HeapNodeStatus.AVAILABLE;
                        _heap[child].resetOwner();
                        return;
                    }
                } else if (!_heap[child].isOwner()) {
                    // If the child is not owned by the caller, then the node must have been moved
                    // up by a concurrent removeMin() call. The method simply moves up the tree to
                    // search for its node
                    child = parent;
                }
            } finally {
                _heap[oldChild].unlock();
                _heap[parent].unlock();
            }
        }
        if (child == ROOT_INDEX) {
            _heap[ROOT_INDEX].lock();
            if (_heap[ROOT_INDEX].isOwner()) {
                _heap[ROOT_INDEX]._tag = HeapNodeStatus.AVAILABLE;
                _heap[child].resetOwner();
            }
            _heap[ROOT_INDEX].unlock();
        }
    }

    @Override
    public E removeMin() {
        // Acquire the global lock preventing any other thread from accessing the
        // root node and the most bottom node (the bottom node will be moved to
        // the top
        _heapLock.lock();
        int bottom = --_next;
        _heap[bottom].lock();
        _heap[ROOT_INDEX].lock();
        _heapLock.unlock();

        // The top and bottom nodes are now locked. Special case: check that
        // the queue is not empty
        if (_heap[ROOT_INDEX]._tag == HeapNodeStatus.NOT_IN_USE) {
            _heap[ROOT_INDEX].unlock();
            _heap[bottom].lock();
            return null;
        }

        // Cache the return result form the locked root node
        E item = _heap[ROOT_INDEX]._item;

        // Mark the root node as NOT_IN_USE and unowned, swaps it with the leaf node,
        // and unlocks  the (now not in use) leaf
        _heap[ROOT_INDEX]._tag = HeapNodeStatus.NOT_IN_USE;
        swap(bottom, ROOT_INDEX);
        _heap[bottom].resetOwner();
        _heap[bottom].unlock();

        // If the heap had only one item, then the leaf and the root are the same, so
        // the method checks whether the root has just been marked as NOT_IN_USE.
        // If so, it unlocks the root and returns the item
        if (_heap[ROOT_INDEX]._tag == HeapNodeStatus.NOT_IN_USE) {
            _heap[ROOT_INDEX].unlock();
            return item;
        }

        // Percolate the new root node down the tree until it reaches its proper
        // position (very similar  logic as the sequential implementation in class
        //  SequentialUnboundedPriorityQueue)
        int child = 0;
        int parent = ROOT_INDEX;
        while (parent < _heap.length / 2) {
            // Calculate indices of left and right children
            int left = parent * 2;
            int right = (parent * 2) + 1;

            // The node being percolated down is locked until it reaches its proper position.
            // When we swap two nodes, we lock them both, and swap their fields. At each step,
            // we lock the node’s right and left children.
            _heap[left].lock();
            _heap[right].lock();

            // Which direction should we attempt to sink the parent node?
            // (This code is best understood by looking at a heap and noting the various
            // conditions for moving a node down to the last level. In Java Concurrency
            // doc 3. look at the diagram in page 4 and apply these condition to node 3)
            // If the left child is not in use, we unlock both children and return. If the
            // right child is empty, but the left child has higher priority, then we unlock
            // the right child and examine the left.  Otherwise, we unlock the left child
            // and examine the right
            if (_heap[left]._tag == HeapNodeStatus.NOT_IN_USE) {
                _heap[right].unlock();
                _heap[left].unlock();
                break;
            } else if (_heap[right]._tag == HeapNodeStatus.NOT_IN_USE ||
                       _heap[left]._priority < _heap[right]._priority) {
                _heap[right].unlock();
                child = left;
            } else {
                _heap[left].unlock();
                child = right;
            }
            if (_heap[child]._priority < _heap[parent]._priority) {
                swap(parent, child);
                _heap[parent].unlock();
                parent = child;
            } else {
                _heap[child].unlock();
                break;
            }
        }
        _heap[parent].unlock();
        return item;
    }


    private void swap(int i, int j) {
        long _owner = _heap[i]._threadId;
        _heap[i]._threadId = _heap[j]._threadId;
        _heap[j]._threadId = _owner;
        E _item = _heap[i]._item;
        _heap[i]._item = _heap[j]._item;
        _heap[j]._item = _item;
        int _priority = _heap[i]._priority;
        _heap[i]._priority = _heap[j]._priority;
        _heap[j]._priority = _priority;
        HeapNodeStatus _tag = _heap[i]._tag;
        _heap[i]._tag = _heap[j]._tag;
        _heap[j]._tag = _tag;
    }

    private void resize() {
        // Make a copy of the existing heap
        HeapNode<E>[] copy = Arrays.copyOf(_heap, _heap.length);

        // Allocate a new bigger heap
        _heap = (HeapNode<E>[])new HeapNode[_heap.length * 2];

        // Copy old heap data to the new heap
        IntStream.range(0, copy.length)
                .forEach(index -> _heap[index] = copy[index]);
    }
}
