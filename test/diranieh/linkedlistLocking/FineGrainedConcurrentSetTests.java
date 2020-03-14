package diranieh.linkedlistLocking;

import diranieh.utilities.Set;

class FineGrainedConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {
    @Override
    public Set<String> createSet() {
        return new FineGrainedConcurrentSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        FineGrainedConcurrentSet<String> set  = new FineGrainedConcurrentSet<>();
        for (String item: items) {
            set.add(item);
        }
        return set;
    }
}