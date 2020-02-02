package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicStampedReference;

public class UnboundedConcurrentLockFreeQueueWithRecycle {

    private void foo() {
        AtomicStampedReference<Integer> stampedReference = new AtomicStampedReference<>(1, 1);

    }
}
