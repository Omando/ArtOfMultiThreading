package diranieh.workDistribution.workStealing;

/** Shows the basic implementation of a bounded thread-unsafe dequeue
 * The implementation in BoundedSequentalDequeue  is used as a starting point when
 * implementing {@link BoundedConcurrentDequeue}
 * */
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

    public E popTop() {
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");
        E item = _items[_indexTop];
        _indexTop++;

        // Special case: If popTop was called an equal number of times as pushBottom,
        // then indices aligns and we can reset both to zero
        if(_indexBottom == _indexTop) {
            _indexBottom = _indexTop = 0;
        }
        return item;
    }

    public E popBottom() {
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");

        E item = _items[_indexBottom - 1];
        _indexBottom--;
        return item;
    }

    public boolean isEmpty() {
        return _indexTop == _indexBottom;
    }

    public int get_Capacity() {
        return _items.length;
    }

    public int get_indexTop() {
        return _indexTop;
    }

    public int get_indexBottom() {
        return _indexBottom;
    }
}
