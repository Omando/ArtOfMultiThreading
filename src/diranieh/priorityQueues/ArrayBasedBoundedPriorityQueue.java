package diranieh.priorityQueues;

import diranieh.concurrentStacks.ConcurrentLockFreeStack;

/** Array-based, thread-safe bounded priority queue.
 * A bounded-range priority queue is one where each item’s score (priority)
 * is taken from the range 0, …, m-1 where m is the range. An unbounded-range
 * priority queue is one where scores are taken from a very large set, say
 * 32-bit integers, or  floating-point values
 * .
 * This implementation we use an array of ConcurrentLockFreeStack<E> as the
 * underlying bounded pool with range of priorities from 0 to m-1
 * */
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
