package diranieh.linkedlistLocking;

class SimpleLinkedListSetTests implements SequentialSetTests {
    @Override
    public Set<String> createSet() {
        return new SimpleLinkedListSet<>();
    }
}