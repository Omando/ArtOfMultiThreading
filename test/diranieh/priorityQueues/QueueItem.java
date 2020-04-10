package diranieh.priorityQueues;

public class QueueItem {
    private String item;
    private int priority;

    public QueueItem(String item, int priority) {
        this.item = item;
        this.priority = priority;
    }

    public String getItem() {
        return item;
    }

    public int getPriority() {
        return priority;
    }
}
