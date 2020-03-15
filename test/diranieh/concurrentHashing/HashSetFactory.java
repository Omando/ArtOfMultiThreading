package diranieh.concurrentHashing;

import diranieh.concurrentHashing.openaddress.CuckooHashSet;

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

    public static <E>CuckooHashSet<E> getOpenAddressNonThreadSafeCuckoo(int capacity) {
        return new CuckooHashSet<E>(capacity);
    }
}
