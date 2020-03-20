package diranieh.concurrentHashing.openaddress;

import diranieh.utilities.Set;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseCuckooHashSet<E>  implements Set<E> {
    final static int CLEAR_MSB = 0x7FFFFFFF;
    final static int ADD_RETRY_LIMIT = 32;
    private final Lock lock;
    private final E[][] tables;      // an array of tables. Initialized in ctor to array of 2 tables.
    private final int _prime;       // Used in MAD hashing. See ctor
    private final int _shift;       // "
    private final int _scale;       // "
    private  int capacity;          // Length of each table
    private int size;               // Count of items

    public CoarseCuckooHashSet(int capacity) {
        lock = new ReentrantLock();
        this.capacity = capacity;
        this.tables = (E[][])new Object[2][capacity];    // an array of 2 tables. Each table has <capacity> entries

        // Required for MAD (multiple-add-divide) hashing. See hash2 function
        Random random = new Random();
        _prime = 433494437;     // Large prime
        _scale = random.nextInt(_prime - 1) + 1;
        _shift =random.nextInt(_prime - 1);
    }

    @Override
    public boolean add(E item) {
        return false;
    }

    @Override
    public boolean remove(E item) {
        return false;
    }

    @Override
    public boolean contains(E item) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
