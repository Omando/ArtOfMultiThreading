package diranieh.concurrentHashing.openaddress;

import diranieh.utilities.Set;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseCuckooHashSet<E>  implements Set<E> {
    final static int CLEAR_MSB = 0x7FFFFFFF;
    final static int ADD_RETRY_LIMIT = 16;
    private final Lock lock;
    private final E[][] tables;      // an array of tables. Initialized in ctor to array of 2 tables.
    private final int _prime;       // Used in MAD hashing. See ctor
    private final int _shift;       // "
    private final int _scale;       // "
    private  int capacity;          // Length of each table
    private int size;               // Count of items


    public CoarseCuckooHashSet(int capacity) {
        this.capacity = capacity;
        this.tables = (E[][])new Object[2][capacity];    // an array of 2 tables. Each table has <capacity> entries
        this.lock = new ReentrantLock();

        // Required for MAD (multiple-add-divide) hashing. See hash2 function
        Random random = new Random();
        _prime = 433494437;     // Large prime
        _scale = random.nextInt(_prime - 1) + 1;
        _shift =random.nextInt(_prime - 1);
    }

    @Override
    public boolean contains(E item) {
        lock.lock();
        try {
            // Is item in the first table?
            if (isItemInFirstTable(item))
                return true;

            if (isItemInSecondTable(item))
                return true;

            // Item not found
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(E item) {
        lock.lock();
        try {
            int iterationCount = 0;
            return add(item, 0, item, iterationCount);
        } finally {
            lock.unlock();
        }
    }

    private boolean add(E item, int tableIndex, E originalItem, int iterationCount) {
        // Resize the tables if we have reached a pre-determined iteration count
        if (iterationCount == ADD_RETRY_LIMIT) {
            iterationCount = 0;
            resize();
        }

        // Determine hash code of given item and determine if it exists in the given table
        int newItemHashCode =( tableIndex == 0)? hash1(item) : hash2(item);
        E existingItem = tables[tableIndex][newItemHashCode];

        // Is item's location already occupied? If not, add them item and we are done
        if (existingItem == null) {
            tables[tableIndex][newItemHashCode] = item;
            size++;
            return true;        // Exit condition for recursive call
        }

        // If we have reached back to the original item, then we have a cycle
        // and we need to exist
        if (existingItem == item)
            return false;

        // Item's location already occupied. Replace the existing item with the new item
        // at this location, and attempt to re-add the existing item to the other table
        tables[tableIndex][newItemHashCode] = item;
        int alternateTableIndex = (tableIndex == 0)? 1: 0;  // Alternate between tables 0 and 1
        return add(existingItem, alternateTableIndex, item, ++iterationCount);
    }

    @Override
    public boolean remove(E item) {
        lock.lock();
        try {
            // Determine hash code of given item and determine if it exists in any of the two tables
            for (int i  = 0; i < 2; i++) {
                int itemHashCode = (i == 0)? hash1(item) : hash2(item);
                if (item.equals(tables[i][itemHashCode])) {
                    tables[i][itemHashCode] = null;
                    size--;
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        // TODO: Is this locking really required?
        lock.lock();
        try {
            return size == 0;
        } finally {
            lock.unlock();
        }
    }

    private int hash1(E item) {
        return (item.hashCode() & CLEAR_MSB)  % capacity;
    }

    /* Multiply, Add, Divide (MAD) method maps a hash code i to [(ai+b) mod p] mod N, Wwhere
        N =  the size of the bucket array
        p = a prime number larger than N
        a = scale factor. Integer chosen at random from the interval [1, p−1]
        b = shift. Integer chosen at random from the interval [0, p−1]
    */
    private int hash2(E item) {
        return ((((item.hashCode() * _scale + _shift) & CLEAR_MSB)  % _prime) % capacity) ;
    }

    private boolean isItemInFirstTable(E item) {
        E[] firstTable = tables[0];
        int hash1Value = hash1(item);
        return (item.equals(firstTable[hash1Value]));
    }

    private boolean isItemInSecondTable(E item) {
        E[] secondTable = tables[1];
        int hash2Value = hash2(item);
        return (secondTable[hash2Value] == item);
    }

    private void resize() {

        // Calculate new size
        int oldSize = tables[0].length;
        capacity = 2 * oldSize;

        // Allocate new table
        E[][] newTables = (E[][])new Object[2][capacity];

        // Re-hash existing values into new table
        Arrays.stream(tables[0]).filter(item -> item != null).forEach(item -> newTables[0][hash1(item)] = item);
        Arrays.stream(tables[1]).filter(item -> item != null).forEach(item -> newTables[1][hash2(item)] = item);

        tables[0] = newTables[0];
        tables[1] = newTables[1];
    }
}
