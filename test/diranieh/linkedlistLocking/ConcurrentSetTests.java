package diranieh.linkedlistLocking;

import org.junit.jupiter.api.Test;

public interface ConcurrentSetTests {
    Set<String> createConcurrentSet();

    @Test
    default void test1() {

    }
}
