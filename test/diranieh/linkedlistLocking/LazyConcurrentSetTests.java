package diranieh.linkedlistLocking;

class LazyConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {

    @Override
    public Set<String> createSet() {
        return new LazyConcurrentSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        LazyConcurrentSet<String> set = new LazyConcurrentSet<>();
        for (String item: items) {
            set.add(item);
        }

        return set;
    }
}