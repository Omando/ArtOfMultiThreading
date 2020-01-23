package diranieh.linkedlistLocking;

public class NonBlockingConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {
    @Override
    public Set<String> createSet() {
        return new NonBlockingConcurrentSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        NonBlockingConcurrentSet<String> set = new NonBlockingConcurrentSet<>();
        for (String item: items) {
            set.add(item);
        }
        return set;
    }
}
