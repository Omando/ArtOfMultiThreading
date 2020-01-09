package diranieh.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final AtomicInteger counter = new AtomicInteger();

    public int increment() {
        return counter.incrementAndGet();
    }

    public void clear() {
        counter.set(0);
    }

    public int get() {
        return counter.get();
    }
}


