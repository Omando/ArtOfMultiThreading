package diranieh.priorityQueues;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PriorityQueueStepDefinitions implements En {
    private  int _threadCount;
    private int _range;
    private PriorityQueue<String> _pq;
    List<String> _removedItems;
    Map<String, Integer> _queueItems;

    public PriorityQueueStepDefinitions() {

        DataTableType((Map<String, String> row) -> new QueueItem(
                row.get("item"),
                Integer.parseInt(row.get("priority"))
        ));

        Given("priority range is {int}", (Integer range) -> _range = range);

        And("priority queue implementation is {string}", (String implementation) -> {
            switch (implementation) {
                case "ArrayBased":
                    _pq = new ArrayBasedBoundedPriorityQueue<>(_range);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported priority queue implementation");
            }
        });

        When("I add the following items to the priority queue", (DataTable rawData) -> {
            /* Three approaches to converting rowData DataTable to a list of QueueItem */
            // First approach: DataTable.asLists()
            List<List<String>> lists = rawData.asLists();
            List<QueueItem> queueItems1 = lists.stream()
                    .skip(1)        // Ignore first row which contains header details
                    .map(row -> new QueueItem(row.get(0), Integer.parseInt(row.get(1))))
                    .collect(Collectors.toList());

            // Second approach: DataTable.asMaps
            Map<String, Integer> itemToPriority = getQueueItems(rawData);

            // Third approach: DataTableType - see next method

            // Add items to the priority queue (using data from the second approach)
            itemToPriority.forEach((item, priority) ->  _pq.add(item, priority));
        });

        // java.lang.ClassCastException: class io.cucumber.datatable.DataTable cannot be cast to
        // class java.util.List
        /*When("I add the following items to the priority queue", (List<QueueItem> data) -> {
            // Add the items to the queue
            data.stream().forEach(item -> pq.add(item.getItem(), item.getPriority()));
        });*/

        And("I call removeMin {int} times", (Integer count) -> {
            _removedItems = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                _removedItems.add(_pq.removeMin());
            }
        });

        Then("I should get these items in this order", (DataTable data) -> {
            List<String> prioritizedItems = data.asList(String.class);

            Assert.assertSame(_removedItems.size(), prioritizedItems.size());
            for (int i = 0; i < _removedItems.size(); i++)
                Assert.assertEquals(_removedItems.get(i), prioritizedItems.get(i));
        });

        And("there are {int} threads acting on the priority queue", (Integer threadCount) -> _threadCount = threadCount);

        When("each thread adds the following items to the priority queue$", (DataTable rawData) -> {

            // Get list of items that each thread will add
            _queueItems = getQueueItems(rawData);

            // Invoke <threadCount> threads with each thread adding the same items
            final CountDownLatch latch = new CountDownLatch(1);
            Thread[] threads = new Thread[_threadCount];

            // Create required number of threads with each thread all items in queueItems list
            for (int i = 0; i < _threadCount; i++) {
                threads[i] = new Thread( () -> {
                    try {
                        // Wait for a signal from the main test thread so that all  threads
                        // start adding items concurrently
                        latch.await();
                        _queueItems.forEach((item, priority) ->  _pq.add(item, priority));
                    } catch (InterruptedException exception) {
                        System.out.println("Error adding items due to interruption: " + exception.getMessage());
                        Thread.currentThread().interrupt();     // restore interrupt status
                    }
                    catch (Exception exception) {
                        System.out.println("Exception adding: " + exception.getMessage());
                    }
                });

                // Schedule all threads
                threads[i].start();
            }

            // Start all threads
            latch.countDown();

            // Wait for all threads to finish
            for (int i = 0; i < _threadCount; i ++) {
                threads[i].join();
            }
            System.out.println("All threads completed adding items...");
        });

        And("threads remove items until the priority queue is empty", () -> {
            _removedItems = new CopyOnWriteArrayList<>();
            final CountDownLatch latch = new CountDownLatch(1);
            Thread[] threads = new Thread[_threadCount];

            // Invoke <threadCount> threads with each thread removing items
            // until all items are removed
            // Create required number of threads with each thread all items in queueItems list
            for (int i = 0; i < _threadCount; i++) {
                threads[i] = new Thread( () -> {
                    try {
                        // Wait for a signal from the main test thread so that all  threads
                        // start adding items concurrently
                        latch.await();
                        String item;
                        int lastKey = Integer.MIN_VALUE;
                        while ((item = _pq.removeMin()) != null) {
                            _removedItems.add(item);
                            int key = _queueItems.get(item);
                            Assert.assertTrue(key > lastKey);
                        }
                        // Thread completes if there are no more items to retrieve.

                    } catch (InterruptedException exception) {
                        System.out.println("Error adding items due to interruption: " + exception.getMessage());
                        Thread.currentThread().interrupt();     // restore interrupt status
                    }
                    catch (Exception exception) {
                        System.out.println("Exception adding: " + exception.getMessage());
                    }
                });

                // Schedule all threads
                threads[i].start();
            }

            // Start all threads
            latch.countDown();

            // Wait for all threads to finish
            for (int i = 0; i < _threadCount; i ++) {
                threads[i].join();
            }
            System.out.println("All threads completed removing items...");

        });

        Then("I should get these items in this order and each item repeated {int} times", (Integer count, DataTable rawData) -> {

            // Get list of items that should have been added by all threads
            List<String> expectedRemovedItems = rawData.asList(String.class);

            // For all removed items, get count of identical items
            Map<String, Long> removedItemsToItemCount =  _removedItems.stream()
                    .collect(Collectors.groupingBy( Function.identity(), Collectors.counting()));

            for (String item: expectedRemovedItems) {
                Long itemCount =  removedItemsToItemCount.get(item);
                Assert.assertEquals((long) itemCount, (long) count);
            }
        });
    }

    private Map<String, Integer> getQueueItems(DataTable rawData) {
        List<Map<String, String>> maps = rawData.asMaps(String.class, String.class);
        Map<String, Integer> priorityByItem =  maps.stream()
                .map(row -> new QueueItem(row.get("item"), Integer.parseInt(row.get("priority"))))
                .collect(Collectors.toMap(p -> p.getItem(), p -> p.getPriority()));
        return priorityByItem;
    }
}
