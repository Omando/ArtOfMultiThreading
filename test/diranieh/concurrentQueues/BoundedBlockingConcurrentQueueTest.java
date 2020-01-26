package diranieh.concurrentQueues;

class BoundedBlockingConcurrentQueueTest implements SequentialQueueTests, ConcurrentQueueTests {
    @Override
    public Queue<Integer> createQueue(int capacity) {
        return new BoundedBlockingConcurrentQueue<Integer>(capacity);
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacity, Iterable<Integer> items) throws InterruptedException {
        BoundedBlockingConcurrentQueue<Integer> queue = new BoundedBlockingConcurrentQueue<>(capacity);
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }
}