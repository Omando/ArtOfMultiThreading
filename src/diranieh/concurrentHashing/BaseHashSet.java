package diranieh.concurrentHashing;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseHashSet<E> {
    protected List<E>[] table;
    protected int size;

    public BaseHashSet(int capacity) {

        // Count of items is initially zero
        size = 0;

        // Create a hash table with the required capacity
        table = new List[capacity];

        // Each hash table entry (bucket) is initialized as an empty list
        for (int i = 0; i < capacity ; i++) {
            table[i] = new ArrayList<>();
        }
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

            // Add the item
            added = table[hashCode].add(item);

            // Update size if item was added
            size = (added)? size+1 : size;

        } finally {
            release(item);
        }

        // Check if we need to adjust the size
        if (added && policy())
            resize();

        return added;
    }

    public boolean remove(E item) {
        acquire(item);
        try {
            // Calculate hash to identify bucket index within table
            int hashCode = calculateHashCode(item);

            // Remove the item
            boolean removed = table[hashCode].remove(item);

            // Update size if item was added
            size = (removed)? size-1 : size;
            return removed;
        } finally {
            release(item);
        }
    }

    public abstract void acquire(E x);

    public abstract void release(E x);

    public abstract void resize();

    public abstract boolean policy();

    private int calculateHashCode(E item) {
        return Math.abs(item.hashCode() % table.length);
    }
}
