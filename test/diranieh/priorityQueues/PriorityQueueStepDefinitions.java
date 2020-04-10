package diranieh.priorityQueues;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriorityQueueStepDefinitions implements En {
    private int _range;
    private PriorityQueue<String> pq;
    List<String> removedItems;

    public PriorityQueueStepDefinitions() {

        DataTableType((Map<String, String> row) -> new QueueItem(
                row.get("item"),
                Integer.parseInt(row.get(1))
        ));

        Given("priority range is {int}", (Integer range) -> {
            _range = range;
        });

        And("priority queue implementation is {string}", (String implementation) -> {
            switch (implementation) {
                case "ArrayBased":
                    pq = new ArrayBasedBoundedPriorityQueue<String>(_range);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported priority queue implementation");
            }
        });

        When("I add the following items to the priority queue", (DataTable rawData) -> {

            /* Three approaches to converting rowData DataTable to a list of QueueItem */
            // First approach: DataTable.asLists()
            // Ignore first row which contains header details
            List<QueueItem> queueItems1 = new ArrayList<>();
            List<List<String>> lists = rawData.asLists();
            for (int i = 1; i < lists.size(); i++) {
                QueueItem item = new QueueItem(lists.get(i).get(0), Integer.parseInt(lists.get(i).get(1)));
                queueItems1.add(item);
            }

            // Second approach: DataTable.asMaps
            List<QueueItem> queueItems2 = new ArrayList<>();
            List<Map<String, String>> maps = rawData.asMaps(String.class, String.class);
            maps.forEach(map -> {
                QueueItem item = new QueueItem(map.get("item"), Integer.parseInt(map.get("priority")));
                queueItems2.add( item);
            });
            queueItems2.stream().forEach(item -> pq.add(item.getItem(), item.getPriority()));

            // Third approach: DataTableType
            /*List<QueueItem> queueItems3 = new ArrayList<>();
            queueItems3 = rawData.asList(QueueItem.class);

            // Add the items to the queue
            queueItems3.stream().forEach(item -> pq.add(item.getItem(), item.getPriority()));*/
        });

        // Arrays.asList fails with ClassCastException
        /*When("I add the following items to the priority queue", (QueueItem[] data) -> {
            List<QueueItem> queueItems3 = Arrays.asList(data);

            // Add the items to the queue
            queueItems3.stream().forEach(item -> pq.add(item.getItem(), item.getPriority()));
        });*/

        And("I call removeMin {int} times", (Integer count) -> {
            removedItems = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                removedItems.add(pq.removeMin());
            }
        });
        Then("I should get these items in this order", (DataTable data) -> {
            List<String> prioritizedItems = data.asList(String.class);

            Assert.assertSame(removedItems.size(), prioritizedItems.size());
            for (int i = 0; i <removedItems.size(); i++)
                Assert.assertEquals(removedItems.get(i), prioritizedItems.get(i));
        });
    }
}
