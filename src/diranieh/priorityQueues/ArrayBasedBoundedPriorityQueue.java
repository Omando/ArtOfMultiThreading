package diranieh.priorityQueues;

import diranieh.concurrentStacks.ConcurrentLockFreeStack;

/* Array-based, thread-safe bounded priority queue */
public class ArrayBasedBoundedPriorityQueue<E> implements PriorityQueue<E> {
    private final int priorityRange;
    private final ConcurrentLockFreeStack<E>[] pool;

    public ArrayBasedBoundedPriorityQueue(int range) {
        this.priorityRange = range;
        this.pool =  (ConcurrentLockFreeStack<E>[]) new ConcurrentLockFreeStack[range];

        for (int i = 0; i < range; i++)
            pool[i] = new ConcurrentLockFreeStack<>();
    }

    @Override
    public void add(E item, int priority) {
        if (priority >= priorityRange)
            throw new IllegalArgumentException(String.format("priority must be less than %d", priorityRange));

        pool[priority].push(item);
    }

    // removeMin() method scans the bins in decreasing priority and returns
    // the first item it successfully removes. If no item is found it returns null
    @Override
    public E removeMin() {
        for (int i = 0; i < priorityRange; i++) {
            E item = pool[i].pop();
            if (item != null)
                return item;
        }
        return null;
    }
}
