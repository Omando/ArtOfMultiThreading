package diranieh.linkedlistLocking;

class SimplerLinkedListSetTests implements SequentialSetTests {
    @Override
    public Set<String> createSet() {
        return new SimplerLinkedListSet<>();
    }
}