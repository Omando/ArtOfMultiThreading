package diranieh.concurrentHashing.openaddress;

import diranieh.utilities.Set;

public class CoarseCuckooHashSet<E>  implements Set<E> {
    @Override
    public boolean add(E item) {
        return false;
    }

    @Override
    public boolean remove(E item) {
        return false;
    }

    @Override
    public boolean contains(E item) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
