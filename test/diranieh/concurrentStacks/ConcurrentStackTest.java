package diranieh.concurrentStacks;

import org.junit.jupiter.api.RepeatedTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface ConcurrentStackTest extends BaseStackTest<Integer> {
    // Multiple threads are used to concurrently push items. Sequential code then
    // pops all item and checks that pushed and popped items are consistent
    @RepeatedTest(100)
    default void should_push_concurrently() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Stack<Integer> stack = createStack();
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] threads = new Thread[THREAD_COUNT];

        // Create THREAD_COUNT threads with each thread populating the stack
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int startValue = i * ITEMS_PER_THREAD;
            threads[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads enqueue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        stack.push(startValue + j);
                    }
                } catch (InterruptedException exception) {
                    System.out.println("Error pushing into stack: " + exception.getMessage());
                    Thread.currentThread().interrupt();     // restore interrupt status
                }
            });
            threads[i].start();
        }

        // Start enqueueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT; i ++) {
            threads[i].join();
        }

        // Now check results by dequeueing all items sequentially and flag any duplicates
        for (int i = 0; i < TEST_SIZE; i++) {
            int j = stack.pop();
            if (poppedItems[j]) {
                fail("duplicate pop: " + j);
            } else {
                poppedItems[j] = true;
            }
        }
    }

    // Sequential code pushes many items the multiple threads are used to
    // concurrently pop items
    @RepeatedTest(100)
    default void should_pop_concurrently() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        boolean[] poppedItems = new boolean[TEST_SIZE];
        Thread[] threads = new Thread[THREAD_COUNT];


        final Stack<Integer> stack = createStack();
        List<Integer> numbers = List.of(IntStream.range(0, TEST_SIZE).boxed().toArray(Integer[]::new));
        numbers.forEach(number -> stack.push(number));

        // Create THREAD_COUNT threads with each thread dequeueing the queue
        // with ITEMS_PER_THREAD items
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread( () -> {
                // Wait for signal from main test thread so that all threads dequeue concurrently
                try {
                    latch.await();
                    for (int j = 0; j < ITEMS_PER_THREAD; j++) {
                        int item = stack.pop();
                        if (poppedItems[item]) {
                            fail("duplicate pop: " + j);
                        } else {
                            poppedItems[item] = true;
                        }
                    }
                } catch (InterruptedException exception) {
                    System.out.println("Error dequeueing: " + exception.getMessage());
                    Thread.currentThread().interrupt();     // restore interrupt status
                }
            });
            threads[i].start();
        }

        // Start dequeueing concurrently
        latch.countDown();

        // Wait for all threads to finish
        for (int i = 0; i < THREAD_COUNT; i ++) {
            threads[i].join();
        }

        // Now check results by dequeueing all items sequentially and flag any duplicates
        for (int i = 0; i < TEST_SIZE; i++) {
            assertTrue(poppedItems[i]);
        }
    }
}
