package diranieh.playground;

import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class PlaygroundTests {
    volatile boolean stop = false;

    // Test only succeeds when stop is declared volatile
    @RepeatedTest(10)
    public void test() throws InterruptedException {
        // Start a thread that monitor stop variable
        stop = false;

        Thread thread = new Thread(() -> {
            while (!stop) {
                // Do some work
                double value;
                for (int i = 0; i < 100; ++i)
                    value = Math.sin(i * Math.PI / 180);
            }
            System.out.println("Thread detected stop signal");
        });
        thread.start();

        // Give thread some time to get scheduled and executed
        Thread.sleep(100);

        // Tell thread to stop. Not visible to another thread running on
        // another core unless stop is volatile.
        stop = true;

        // Wait for thread to stop
        thread.join(1000);
        assertFalse(thread.isAlive());
    }

}
