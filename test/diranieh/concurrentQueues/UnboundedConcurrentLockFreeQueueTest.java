package diranieh.concurrentQueues;

class UnboundedConcurrentLockFreeQueueTest implements SequentialQueueTests, ConcurrentQueueTests {

    @Override
    public Queue<Integer> createQueue(int capacity) {
        return new UnboundedConcurrentLockFreeQueue<>();
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacity, Iterable<Integer> items) throws InterruptedException {
        var queue = new UnboundedConcurrentLockFreeQueue<Integer>();
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }
}