package diranieh.concurrentHashing;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class stepDefinitions implements En {
    private final HashSetFactory<Integer> factory = new HashSetFactory<Integer>();
    private Integer capacity;
    private Integer threshold;
    private Integer itemsPerThread;
    private Integer threadCount;
    private boolean[] items;
    private BaseHashSet<Integer> hashSet;


    public stepDefinitions() {
        Given("capacity is {int} and bucket threshold is {int}",
                (Integer capacity, Integer threshold) -> {
            this.capacity = capacity;
            this.threshold = threshold;
        });

        Given("implementation is {string}", (String implementationName) -> {
            switch (implementationName) {
                case "Coarse":
                    hashSet = factory.getCoarse(capacity, threshold);
                    break;
                case "Striped":
                    hashSet = factory.getStriped(capacity, threshold);
                case "Refined":
                    hashSet = factory.getRefined(capacity, threshold);
            }
        });

        When("I add the following items", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            numbers.forEach(number -> hashSet.add(number) );
        });

        And("I remove the following items", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            numbers.forEach(number -> hashSet.remove(number) );
        });

        Then("only these items should exist", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            Assertions.assertEquals(2, hashSet.size.get());
            numbers.forEach(number -> {
                Assertions.assertTrue( hashSet.contains(number));
            });
        });

        When("I add {int} numbers", (Integer itemCount) -> {
            for (int i = 0; i < itemCount; i++) {
                hashSet.add(i);
            }
        });

        Then("capacity should increase to {int}", (Integer newCapacity) -> {
            Assertions.assertEquals(newCapacity, hashSet.table.length);
        });

        /* Concurrent access */
        Given("There are {int} threads", (Integer threadCount) -> {
            this.threadCount = threadCount;
        });

        Given("Each thread adds {int} new items", (Integer itemsPerThread) -> {
            this.itemsPerThread = itemsPerThread;
        });

        When("multiple threads add", () -> {
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

        Then("total item count is {int}", (Integer totalItemCount) -> {
            assertEquals(totalItemCount, hashSet.size.get());
        });

        And("all items are added from all threads", () -> {
            // should have added all items from all threads
            for (int i = 0; i < (this.threadCount * this.itemsPerThread); i++) {
                assertTrue(items[i], "Missing value " + i);
            }
        });
    }
}
