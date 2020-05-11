package diranieh.concurrentHashing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class implementation for closed-addressing hash set implementations
 *
 * Note regarding calculating hash codes:
 * The mod operator returns a non-positive integer if its first argument is negative;
 * this may happen if type E's user-supplied hashCode() was not guaranteed to always
 * return a positive value. The result of the mod operator in this case throws an out-
 * of-bounds exception. One work around is to use Math.Abs(x) mod M, but the absolute
 * value function can even return a negative integer (1 in 4 Billion!). This happens if
 * its argument is Integer.MIN_VALUE because the resulting positive integer cannot be
 * represented using a 32-bit two's complement integer.
 *
 * We therefore ensure the hash code is always positive by ANDing it with 0x7FFF FFFF.
 * 0x7FFF FFFF in binary is 0111 1111 1111 1111 1111 1111 1111 1111 (all 1s except the sign
 * bit). This means:
 *      hash & 0x7FFFFFFF gives a positive integer.
 *      (hash & 0x7FFFFFFF) mod (array.length - 1) gives a positive integer within array bounds
 *
 * @param <E> the type of the elements in the list
 */
public abstract class BaseHashSet<E> {
    final int CLEAR_MSB = 0x7FFFFFFF;
    // The underlying data structure is an array of lists
    protected List<E>[] table;
    protected AtomicInteger size;

    /* Lock striping ensures synchronized access to individual hashtable buckets,
    but the size parameter is a shared variable modified by different threads that
    must be protected, hence the use of an AtomicInteger  */
    public BaseHashSet(int initialCapacity) {

        // Count of all items is initially zero
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
        return (item.hashCode() & CLEAR_MSB) % table.length;
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
