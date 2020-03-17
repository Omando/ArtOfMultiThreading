package diranieh.concurrentHashing.openaddress;

import diranieh.utilities.Set;

import java.util.Arrays;
import java.util.Random;

/**
 * Implement Cuckoo hashing, a sequential hashing algorithm in which a newly added item
 * displaces any earlier item occupying the same slot
 *
 * Note regarding calculating hash codes:
 * The mod operator returns a non-positive integer if its first argument is negative;
 * this may happen if E's user-supplied hashCode() was not guaranteed to always return
 * a positive value. The result of the mod operator in this case throws an out-of-bounds
 * exception. One work around is to use Math.Abs(x) mod M, but the absolute value function
 * can even return a negative integer (1 in 4 Billion!). This happens if its argument is
 * Integer.MIN_VALUE because the resulting positive integer cannot be represented using a
 * 32-bit two's complement integer.
 *
 * We therefore ensure the hash code is always positive by ANDing it with 0x7FFF FFFF.
 * 0x7FFF FFFF in binary is 0111 1111 1111 1111 1111 1111 1111 1111 (all 1s except the sign
 * bit). This means:
 *      hash & 0x7FFFFFFF gives a positive integer.
 *      (hash & 0x7FFFFFFF) mod (array.length - 1) gives a positive integer within array bounds
 *
 * @param <E> the type of the elements in the list
 */
public class CuckooHashSet<E>  implements Set<E> {
    final static int CLEAR_MSB = 0x7FFFFFFF;
    final static int ADD_RETRY_LIMIT = 32;
    private final E[][] tables;      // an array of tables. Initialized in ctor to array of 2 tables.
    private final int _prime;       // Used in MAD hashing. See ctor
    private final int _shift;       // "
    private final int _scale;       // "
    private  int capacity;          // Length of each table
    private int size;               // Count of items


    public CuckooHashSet(int capacity) {
        this.capacity = capacity;
        this.tables = (E[][])new Object[2][capacity];    // an array of 2 tables. Each table has <capacity> entries

        // Required for MAD (multiple-add-divide) hashing. See hash2 function
        Random random = new Random();
        _prime = 433494437;     // Large prime
        _scale = random.nextInt(_prime - 1) + 1;
        _shift =random.nextInt(_prime - 1);
    }

    @Override
    public boolean contains(E item) {
        // Is item in the first table?
        if (isItemInFirstTable(item))
            return true;

        if (isItemInSecondTable(item))
            return true;

        // Item not found
        return false;
    }

    @Override
    public boolean add(E item) {
        int iterationCount = 0;
         return add(item, 0, item, iterationCount);
    }

    private boolean add(E item, int tableIndex, E originalItem, int iterationCount) {
        // Resize the tables if we have reached a pre-determined iteration count
        if (iterationCount == ADD_RETRY_LIMIT)
            resize();

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
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
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
