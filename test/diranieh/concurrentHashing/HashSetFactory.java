package diranieh.concurrentHashing;

import diranieh.concurrentHashing.openaddress.CoarseCuckooHashSet;
import diranieh.concurrentHashing.openaddress.CuckooHashSet;
import diranieh.utilities.Set;

public class HashSetFactory<E> {
    public static <E> BaseHashSet<E> getCloseAddressCoarse(int capacity, int threshold) {
        return new ConcurrentCoarseHashSet<E>(capacity, threshold);
    }

    public static <E> BaseHashSet<E> getCloseAddressStriped(int capacity, int threshold) {
        return new ConcurrentStripedHashSet<E>(capacity, threshold);
    }

    public static <E> BaseHashSet<E> getCloseAddressRefined(int capacity, int threshold) {
        return new ConcurrentRefinedStripedHashSet<E>(capacity, threshold);
    }

    public static <E> Set<E> getOpenAddressNonThreadSafeCuckoo(int capacity) {
        return new CuckooHashSet<E>(capacity);
    }

    public static <E> Set<E> getOpenAddressCoarseCuckoo(int capacity) {
        return new CoarseCuckooHashSet<E>(capacity);
    }
}
