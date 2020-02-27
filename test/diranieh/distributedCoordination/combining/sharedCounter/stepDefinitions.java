package diranieh.distributedCoordination.combining.sharedCounter;

import diranieh.distributedCoordination.combining.CombiningTree;
import diranieh.utilities.ThreadNumberGenerator;
import io.cucumber.java8.En;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// Note that StepClass must implement cucmber.api.java8.En interface and step
// methods should be inside the constructor of test class.
public class stepDefinitions implements En {
        private CombiningTree sharedCounter;
        private int finalCount;
        private int threadCount;
        private int countsPerThread;

        public stepDefinitions() {
            Before(() -> {
                System.out.println("Resetting thread number generator");
                ThreadNumberGenerator.IndexReset();
            });

            /* Sequential steps*/
            Given("Single threaded", () -> {
                threadCount = 1;
                sharedCounter = new CombiningTree(threadCount);
            });

            When("I increment counter {int} times", (Integer count) -> {
                for(int i = 0; i < count; i++)
                    finalCount = sharedCounter.getAndIncrement();
            });

            Then("count will be {int}", (Integer expectedCount) -> {
                Assertions.assertEquals(expectedCount, finalCount);
            });

            /* Concurrent steps*/
            Given("There are {int} threads", (Integer threadCount) -> {
                this.threadCount = threadCount;
                sharedCounter = new CombiningTree(threadCount);
            });

            Given("Each thread counts {int} times", (Integer countsPerThread) -> {
                this.countsPerThread = countsPerThread;
            });

            When("multiple threads count", () -> {
                final CountDownLatch latch = new CountDownLatch(1);
                Thread[] threads = new Thread[this.threadCount];
                boolean[] counts = new boolean[this.threadCount * this.countsPerThread];

                // Create required number of threads with each thread counting
                // {countsPerThread} times
                for (int i = 0; i < this.threadCount; i++) {
                    threads[i] = new Thread( () -> {
                        // Wait for signal from main test thread so that all
                        // threads count concurrently
                        try {
                            latch.await();
                            for (int j = 0; j < this.countsPerThread; j++) {
                                int prior = sharedCounter.getAndIncrement();
                                if (counts[prior]) {
                                    fail("duplicate count: " + j);
                                } else {
                                    counts[prior] = true;
                                }
                            }
                        } catch (InterruptedException exception) {
                            System.out.println("Error counting due to interruption: " + exception.getMessage());
                            Thread.currentThread().interrupt();     // restore interrupt status
                        }
                        catch (Exception exception) {
                            System.out.println("Exception counting: " + exception.getMessage());
                        }
                    });
                    threads[i].start();
                }

                // Start popping concurrently
                latch.countDown();

                // Wait for all threads to finish
                for (int i = 0; i < this.threadCount; i ++) {
                    threads[i].join();
                }

                // Assertion
                int count = 0;
                for (int i = 0; i < (this.threadCount * this.countsPerThread); i++) {
                    // should have counted all numbers up to test size
                    assertTrue(counts[i], "Missing count value " + i);
                    count = i;
                }
                finalCount = count;
            });

            Then("final count be {int}", (Integer expectedCount) -> {
                Assertions.assertEquals(expectedCount, finalCount);
            });
        }
}
