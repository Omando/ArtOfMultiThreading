package diranieh.concurrentQueues;

class UnboundedConcurrentQueueTest implements SequentialQueueTests, ConcurrentQueueTests {
    @Override
    public Queue<Integer> createQueue(int capacityIgnored) {
        return new UnboundedConcurrentQueue<>();
    }

    @Override
    public Queue<Integer> createAndPopulateQueue(int capacityIgnored, Iterable<Integer> items) throws InterruptedException {
        UnboundedConcurrentQueue<Integer> queue = new UnboundedConcurrentQueue<>();
        for (Integer item: items) {
            queue.enqueue(item);
        }
        return queue;
    }
}