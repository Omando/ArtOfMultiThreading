package diranieh.concurrentQueues;

class UnboundedConcurrentBlockingQueueTest implements SequentialQueueTests, ConcurrentQueueTests {
    @Override
    public Queue<Integer> createQueue(int capacityIgnored) {
        return new UnboundedConcurrentBlockingQueue<>();
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacityIgnored, Iterable<Integer> items) throws InterruptedException {
        UnboundedConcurrentBlockingQueue<Integer> queue = new UnboundedConcurrentBlockingQueue<>();
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }
}