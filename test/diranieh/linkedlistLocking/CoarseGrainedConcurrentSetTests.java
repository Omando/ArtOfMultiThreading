package diranieh.linkedlistLocking;

class CoarseGrainedConcurrentSetTests implements SequentialSetTests {
    @Override
    public Set<String> createSet() {
        return new CoarseGrainedConcurrentSet<>();
    }
}