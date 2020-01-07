package diranieh.linkedlistLocking;

class CoarseGrainedConcurrentSetTests implements SequentialSetTests, ConcurrentSetTests {
    @Override
    public Set<String> createSet() {
        return new CoarseGrainedConcurrentSet<>();
    }

    @Override
    public Set<String> createConcurrentSet() {
        return new CoarseGrainedConcurrentSet<>();
    }
}