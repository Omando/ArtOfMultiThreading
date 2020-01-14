package diranieh.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class NonBlockingCounter {
    private  AtomicInteger count = new AtomicInteger();

    public int getValue() {
        return count.get();
    }

    public int incrementAndGet() {
        int expectedValue;
        do {
            expectedValue = count.get();
        } while (!  count.compareAndSet(expectedValue, expectedValue + 1));

        return expectedValue + 1;
    }

    public void increment() {
        incrementAndGet();      // Ignore return value
    }
}
