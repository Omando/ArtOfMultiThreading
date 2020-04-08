package diranieh.priorityQueues;

import diranieh.concurrentStacks.ConcurrentLockFreeStack;

public class ArrayBasedBoundedPriorityQueue<E> implements PriorityQueue<E> {
    private final int range;
    private final ConcurrentLockFreeStack<E>[] pool;

    public ArrayBasedBoundedPriorityQueue(int range) {
        this.range = range;
        this.pool =  (ConcurrentLockFreeStack<E>[]) new ConcurrentLockFreeStack[range];

        for (int i = 0; i < range; i++)
            pool[i] = new ConcurrentLockFreeStack<>();
    }

    @Override
    public void add(E item, int priority) {
        // todo
    }

    @Override
    public E removeMin() {
        // todo
        return null;
    }
}
