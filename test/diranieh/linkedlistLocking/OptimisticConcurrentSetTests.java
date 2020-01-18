package diranieh.linkedlistLocking;

public class OptimisticConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {
    @Override
    public Set<String> createSet() {
        return new OptimisticConcurrentSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        OptimisticConcurrentSet<String> set = new OptimisticConcurrentSet<>();
        for (String item: items) {
            set.add(item);
        }
        return set;
    }
}
