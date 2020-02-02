package diranieh.concurrentQueues;

import diranieh.concurrentQueues.withRecycle.UnboundedConcurrentLockFreeQueueWithRecycle;

class UnboundedConcurrentLockFreeQueueWithRecycleTests implements SequentialQueueTests, ConcurrentQueueTests {

    @Override
    public Queue<Integer> createQueue(int capacity) {
        return new UnboundedConcurrentLockFreeQueueWithRecycle<Integer>();
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacity, Iterable<Integer> items) throws InterruptedException {
        var queue = new UnboundedConcurrentLockFreeQueueWithRecycle<Integer>();
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }
}
