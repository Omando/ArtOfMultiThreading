package diranieh.linkedlistLocking;

import diranieh.utilities.Set;

class CoarseGrainedConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {
    @Override
    public Set<String> createSet() {
        return new CoarseGrainedConcurrentSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        CoarseGrainedConcurrentSet<String> set  = new CoarseGrainedConcurrentSet<>();
        for (String item: items) {
            set.add(item);
        }
        return set;
    }
}