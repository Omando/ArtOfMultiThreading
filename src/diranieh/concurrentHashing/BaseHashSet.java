package diranieh.concurrentHashing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseHashSet<E> {
    // The underlying data structure is an array of lists
    protected List<E>[] table;
    //protected int size;
    protected AtomicInteger size;

    public BaseHashSet(int initialCapacity) {

        // Count of all items is initially zero
        //size = 0;
        size = new AtomicInteger(0);

        // Create and initialize the underlying hash table
        table = createAndInitializeHashTable(initialCapacity);
    }

    public boolean contains(E item) {
        acquire(item);
        try {
            // Calculate hash to identify bucket index within table
            int hashCode = calculateHashCode(item);

            // Check if item exists in the bucket
            return table[hashCode].contains(item);
        } finally {
            release(item);
        }
    }

    public boolean add(E item) {
        boolean added = false;
        acquire(item);
        try {
            // Calculate hash to identify bucket index within table
            int hashCode = calculateHashCode(item);

            // Add the item if and only if it does not currently exist
            if (!table[hashCode].contains(item)) {
                table[hashCode].add(item);
                //++size;
                size.incrementAndGet();
                System.out.println("Added " + item + " size = " + size);
                added = true;
            }
        } finally {
            release(item);
        }

        // Check if we need to adjust the size
        // This is a check-then-act idiom and is thread-unsafe. See resize() implementation
        // in the derived class where check-then-act is done in a thread-safe manner inside
        // the resize method
        if (shouldResize())
            resize();

        return added;
    }

    public boolean remove(E item) {
        acquire(item);
        try {
            // Calculate hash to identify bucket index within table
            int hashCode = calculateHashCode(item);

            // Remove the item if it already exists
            boolean removed = table[hashCode].remove(item);

            // Update size if item was removed
            if (removed)
                //size--;
                size.decrementAndGet();

            return removed;
        } finally {
            release(item);
        }
    }

    protected abstract void acquire(E x);

    protected abstract void release(E x);

    protected abstract void resize();

    protected abstract boolean shouldResize();

    protected int calculateHashCode(E item) {
        return Math.abs(item.hashCode() % table.length);
    }

    protected List<E>[] createAndInitializeHashTable(int capacity) {
        // Create a hash table with the required capacity
        // Each hash table entry (bucket) is initialized as an empty list
        List<E>[] newTable = (List<E>[])new List[capacity];

        // Each bucket is an un-synchronized List<E>
        for (int i = 0; i < capacity ; i++) {
            newTable[i] = new ArrayList<>();
        }

        return newTable;
    }
}
