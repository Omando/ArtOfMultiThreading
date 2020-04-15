package diranieh.priorityQueues;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SequentialUnboundedPriorityQueue<E> implements  PriorityQueue<E> {
    private HeapNode<E>[] _heap;                 // Heap array. Item at index 0 is not used
    private static final int ROOT_INDEX = 1;    // The root node is at index 1 in the heap array
    private int _next;                          // index of the first unused slot in the heap array

    // Each element in the heap array is a HeapNode object that contains an item and its priority
    private static class HeapNode<E> {
        int priority;
        E item;

        public HeapNode(E item, int priority) {
            this.item = item;
            this.priority = priority;
        }

        /*public void init(E item, int priority) {
            this.item = item;
            this.priority = priority;
        }*/
    }

    public SequentialUnboundedPriorityQueue(int capacity) {
        // We ignore first array entry. This means that a node at index k has left and right
        // children at indices 2k and 2k+1, respectively
        _next = 1;      // Ignore first array entry
        _heap = (HeapNode<E>[]) new HeapNode[capacity + 1];  // +1 because we are ignoring item at index 0

    /*    for (int i = 1; i < capacity + 1; i++) {            // Ignore array item at index 0
            heap[i] = new HeapNode<E>();
        }*/
    }

    // Add an item at the first available heap slot, then "swim up" to balance the tree
    public void add(E item, int priority) {

        // Get location of the next available slot and add a node for the new item there
        //heap[childIndex].init(item, priority);
        int childIndex = _next++;

        // continue here
        if (_next >= _heap.length)
            resize();

        _heap[childIndex] = new HeapNode<>(item, priority);

        // The node for the new item must now swim up the priority queue tree in order
        // to re-balance the tree
        while (childIndex > ROOT_INDEX) {   // Keep on swimming up as long as we have not reached the root

            // Calculate the parent index, then swap child and parent if child has a lower priority
            int parentIndex = childIndex / 2;
            if (_heap[childIndex].priority < _heap[parentIndex].priority) {
                // Swap nodes and swap indexes
                swapNodes(childIndex, parentIndex);
                childIndex = parentIndex;
                continue;
            }

            break;     // Parent has a higher priority than child. We are done
        }
    }

    public E getMin() {
        return _heap[ROOT_INDEX].item;
    }

    public E removeMin() {
        // Cache the highest priority item (this is the return result)
        E item = _heap[ROOT_INDEX].item;

        // Get location of least priority node then swap with the root
        int lastNodeIndex = --_next;
        swapNodes(ROOT_INDEX, lastNodeIndex);

        // Edge case: nothing more to do if the least priority node was the only node in the tree
        if (lastNodeIndex == ROOT_INDEX)
            return item;

        int childIndex = 0;
        int parentIndex = ROOT_INDEX;
        while (parentIndex < _heap.length / 2) {
            int leftChildIndex = parentIndex * 2;
            int rightChildIndex = (parentIndex * 2) + 1;

            // Which direction should we attempt to sink the parent node?
            // (This code is best understood by looking at a heap and noting the various
            // conditions for moving a node down to the last level. In Java Concurrency doc 3
            // look at the diagram in page 4 and apply these condition to node 3)
            if (leftChildIndex >= _next) {
                break;
            } else if (rightChildIndex >= _next || _heap[leftChildIndex].priority < _heap[rightChildIndex].priority) {
                childIndex = leftChildIndex;
            } else {
                childIndex = rightChildIndex;
            }

            // Swap parent and child if parent has a lower priority. If parent has higher
            // priority then we're done
            if (_heap[childIndex].priority < _heap[parentIndex].priority) {
                // Swap nodes and swap indexes
                swapNodes(parentIndex, childIndex);
                parentIndex = childIndex;
                continue;
            }

            break;       // Tree balanced. We're done
        }
        return item;
    }
    private void swapNodes(int i, int j) {
        HeapNode<E> node = _heap[i];
        _heap[i] = _heap[j];
        _heap[j] = node;
    }

    public boolean isEmpty() {
        return _next == 0;
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

