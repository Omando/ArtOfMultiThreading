package diranieh.playground;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class PlaygroundTests {
    volatile boolean stop = false;

    // Test only succeeds when stop is declared volatile
    @RepeatedTest(10)
    void succeeds_when_flag_is_volatile() throws InterruptedException {
        // Start a thread that monitor stop variable
        stop = false;

        Thread thread = new Thread(() -> {
            double value = 0;
            while (!stop) {
                // Do some dummy work
                for (int i = 0; i < 100; ++i)
                    value += Math.sin(i * Math.PI / 180);
            }
            System.out.println(String.format("Thread detected stop signal. Final value: {%.2f}", value));
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

    @Test
    void preAndpost() {
        int a = 4;
        System.out.println("a++ = " + a++);     // output = 4, then a = 5
        System.out.println("++a = " + ++a);     // output = 6, then a = 6

        int x = 4;
        var result1 = x++ + ++x;    // 4 + 6 = 10
        System.out.println(result1);

        x = 4;
        var result2 = x-- - --x;    // 4 - 2 = 2
        System.out.println(x);

        int index = 0;
        int result_post = postIncrement(index);     // result_post = 0;
        int result_pre = preIncrement(index);       // result_pre = 1

    }

    private int postIncrement(int index) {
        return index++;
    }

    private int preIncrement(int index) {
        return ++index;
    }
}
