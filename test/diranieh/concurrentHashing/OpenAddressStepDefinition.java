package diranieh.concurrentHashing;

import diranieh.utilities.Set;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAddressStepDefinition implements En {
    private int capacity;
    private int itemCount;
    private Set<Integer> hashSet;
    private Integer itemsPerThread;
    private Integer threadCount;
    private boolean[] items;
    private int size;

    public OpenAddressStepDefinition() {

        // Could not get this to work!
        /*ParameterType("integerList", "(-?[0-9]+(,\\\\\\\\s*-?[0-9]+)*)",  (String numbers) -> {
            return Arrays.stream(numbers.split(","))
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
                });*/

        Given("capacity is {int}", (Integer capacity) -> {
            this.capacity = capacity;
        });

        And("open address implementation is {string}", (String implementation) -> {
            switch (implementation) {
                case "ThreadUnSafe":
                    hashSet = HashSetFactory.getOpenAddressNonThreadSafeCuckoo(capacity);
                    break;
                case "Coarse":
                    hashSet = HashSetFactory.getOpenAddressCoarseCuckoo(capacity);
            }
        });

        When("I add {string}", (String addedNumbers) -> {
            Arrays.stream(addedNumbers.split(","))
                    .filter( s -> !s.isEmpty())
                    .map(s -> Integer.parseInt(s))
                    .forEach(number -> hashSet.add(number));
        });

        And("I remove {string}", (String removedNumbers) -> {
            Arrays.stream(removedNumbers.split(","))
                    .filter(Predicate.not(String::isEmpty))
                    .map((Integer::parseInt))
                    .forEach(number -> hashSet.remove(number));
        });

        Then("{string} should exist", (String remainingNumbers) -> {
            if (remainingNumbers.isEmpty())
                Assert.assertTrue(hashSet.isEmpty());
            else
                Arrays.stream(remainingNumbers.split(","))
                    .filter(Predicate.not(String::isEmpty))
                    .map(Integer::parseInt)
                    .forEach(number -> Assert.assertTrue(hashSet.contains(number)));
        });

        When("I add {int} numbers to the list", (Integer count) -> {
            itemCount = count;
            for (int i = 0; i < itemCount; i++) {
                hashSet.add(i);
            }
        });

        And("I remove the same numbers", () -> {
            for (int i = 0; i < itemCount; i++) {
                hashSet.remove(i);
            }
        });

        Then("hash set should be empty", () -> {
            Assert.assertTrue(hashSet.isEmpty());
        });

        And("I create {int} threads", (Integer threadCount) -> {
            this.threadCount = threadCount;
        });
        And("Each of my threads adds {int} new items", (Integer itemsPerThread) -> {
            this.itemsPerThread = itemsPerThread;
        });
        When("all my threads add items", () -> {
            final CountDownLatch latch = new CountDownLatch(1);
            Thread[] threads = new Thread[this.threadCount];
            items = new boolean[this.threadCount * this.itemsPerThread];

            // Create required number of threads with each thread counting
            // {countsPerThread} times
            for (int i = 0; i < this.threadCount; i++) {
                int threadIndex = i;
                threads[i] = new Thread( () -> {
                    // Wait for signal from main test thread so that all
                    // threads count concurrently
                    try {
                        latch.await();
                        int startingValue = (threadIndex * this.itemsPerThread);
                        for (int j = 0; j < this.itemsPerThread; j++) {
                            int valueToAdd = startingValue + j;
                            boolean added = hashSet.add(valueToAdd);
                            if (!added)
                                fail("duplicate item: " + valueToAdd);
                            else {
                                items[valueToAdd] = true;
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
                threads[i].start();
            }

            // Start popping concurrently
            latch.countDown();

            // Wait for all threads to finish
            for (int i = 0; i < this.threadCount; i ++) {
                threads[i].join();
            }
            System.out.println("All threads completed...");

        });
        Then("total hashset item count is {int}", (Integer totalItemCunt) -> {
            assertEquals(totalItemCunt, size);
        });
        And("all items from all threads are added", () -> {
            // should have added all items from all threads
            for (int i = 0; i < (this.threadCount * this.itemsPerThread); i++) {
                assertTrue(items[i], "Missing value " + i);
                size++;
            }
        });
    }
}
