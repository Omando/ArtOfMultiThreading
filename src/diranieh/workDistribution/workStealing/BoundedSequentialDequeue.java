package diranieh.workDistribution.workStealing;

/* Shows the basic implementation of a bounded thread-unsafe dequeue
* Pushing item*/
public class BoundedSequentialDequeue<E> {
    private final E[] _items;

    private int _indexTop;          // Index of first available item from the top of the array
    private int _indexBottom;      // Index of first empty slot FROM the bottom of the array

    public BoundedSequentialDequeue(int capacity) {
        _items = (E[])new Object[capacity];
        _indexTop = 0;
        _indexBottom = 0;
    }
    public void pushBottom(E item) {
        _items[_indexBottom] = item;
        _indexBottom++;
    }

    public E popBottom() {
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");

        E item = _items[_indexBottom - 1];
        _indexBottom--;
        return item;
    }

    public E popTop() {
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");
        E item = _items[_indexTop];
        _indexTop++;
        return item;
    }

    public boolean isEmpty() {
        return _indexTop == _indexBottom;
    }

    public int get_Capacity() {
        return _items.length;
    }
}
