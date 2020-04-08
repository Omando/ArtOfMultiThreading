package diranieh.priorityQueues;

public interface PriorityQueue<E> {
    void add(E item, int priority);
    E removeMin();
}
