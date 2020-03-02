package diranieh.concurrentHashing;

public class HashSetFactory<E> {
    public BaseHashSet<E> getCoarse(int capacity, int threshold) {
        return new ConcurrentCoarseHashSet<>(capacity, threshold);
    }

    public BaseHashSet<E> getStriped(int capacity, int threshold) {
        return new ConcurrentStripedHashSet<>(capacity, threshold);
    }
}
