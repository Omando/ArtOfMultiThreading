package diranieh.linkedlistLocking;

class SimplerLinkedListSetTests implements SequentialSetTests {
    @Override
    public Set<String> createSet() {
        return new SimplerLinkedListSet<>();
    }

    @Override
    public Set<String> createAndPopulateSet(Iterable<String> items) {
        SimplerLinkedListSet<String> set  = new SimplerLinkedListSet<>();
        for (String item: items) {
            set.add(item);
        }
        return set;
    }
}