package diranieh.locks;

import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class locksStepDefinitions implements En {

    private Lock _lock;
    private Integer _threadCount;
    private Integer _incrementCount;
    private Integer _count;

    public locksStepDefinitions() {
        Given("{int} threads are running", (Integer threadCount) -> {
            _threadCount = threadCount;
        });

        And("locking implementation is {string}", (String implementation) -> {
            switch (implementation) {
                case "ALock":
                    _lock = new ALock(_threadCount);
                    break;
                case "BackoffLock":
                    _lock = new BackoffLock();
                    break;
                case "CLHLock":
                    _lock = new CLHLock();
                    break;
                case "MCSLock":
                    _lock = new MCSLock();
            }
        });

        And("Each running thread increments a shared counter {int}", (Integer incrementCount) -> {
            _incrementCount = incrementCount;
        });

        When("multiple threads increment the counter", () -> {
            _count = 0;
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            Thread[] threads = new Thread[_threadCount];

            // Create required number of threads with each thread counting
            for (int i = 0; i < _threadCount; i++) {
                int threadIndex = i;
                threads[threadIndex] = new Thread(() -> {
                    // Wait for signal from main test thread so that all
                    // threads count concurrently
                    try {
                        countDownLatch.await();

                        for (int j = 0; j < _incrementCount; j++) {
                            _lock.lock();
                            try {
                                _count++;
                            } finally {
                                _lock.unlock();
                            }
                        }
                    } catch (InterruptedException exception) {
                        System.out.println("Error counting due to interruption: " + exception.getMessage());
                        Thread.currentThread().interrupt();     // restore interrupt status
                    }
                    catch (Exception exception) {
                        System.out.println("Exception adding: " + exception.getMessage());
                    }
                });
                threads[threadIndex].start();
            }

            // All threads ready to go. Run all threads
            countDownLatch.countDown();

            // Wait for all threads to finish
            for (int i = 0; i < _threadCount; i ++) {
                threads[i].join();
            }
            System.out.println("All threads completed...");
        });

        Then("Final count is {int}", (Integer finalCount) -> {
            Assert.assertEquals(finalCount, _count);
        });
    }
}
