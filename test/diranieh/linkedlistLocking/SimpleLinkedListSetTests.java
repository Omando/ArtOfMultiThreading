package diranieh.linkedlistLocking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLinkedListSetTests {

    private final String item1 = "Abstract";
    private final String item2 = "Base";
    private final String item3 = "Class";
    private final String item4 = "Derived";

    @Test
    void new_set_should_be_empty() {
        // Arrange & Act
        SimpleLinkedListSet<String> simpleSet = new SimpleLinkedListSet<>();

        // Assert
        assertTrue(simpleSet.isEmpty());
    }

    @Test
    void should_add_and_find() {
        // Arrange
        SimpleLinkedListSet<String> simpleSet = new SimpleLinkedListSet<>();

        // ACt
        boolean add1 = simpleSet.add(item1);
        boolean add2 = simpleSet.add(item2);
        boolean add3 = simpleSet.add(item3);

        // Assert
        assertTrue(!simpleSet.isEmpty());
        assertTrue(simpleSet.contains(item1));
        assertTrue(simpleSet.contains(item2));
        assertTrue(simpleSet.contains(item3));
        assertTrue(add1);
        assertTrue(add2);
        assertTrue(add3);
        assertFalse(simpleSet.contains(item4));
    }

    @Test
    void should_remove_exiting() {
        // Arrange
        SimpleLinkedListSet<String> simpleSet = new SimpleLinkedListSet<>();

        // Act
        boolean add1 = simpleSet.add(item1);
        boolean add2 = simpleSet.add(item2);
        boolean add3 = simpleSet.add(item3);
        boolean remove4 = simpleSet.remove(item4);
        boolean remove2 =  simpleSet.remove(item1);
        boolean remove1 = simpleSet.remove(item2);
        boolean remove3 = simpleSet.remove(item3);
        boolean contains1 = simpleSet.contains(item1);
        boolean contains2 = simpleSet.contains(item2);
        boolean contains3 = simpleSet.contains(item3);

        // Assert
        assertTrue(simpleSet.isEmpty());
        assertTrue(add1);
        assertTrue(add2);
        assertTrue(add3);
        assertTrue(remove1);
        assertTrue(remove2);
        assertTrue(remove3);
        assertFalse(remove4);
        assertFalse(contains1);
        assertFalse(contains2);
        assertFalse(contains3);
    }

    @Test
    void should_not_add_existing_item() {
        // Arrange
        SimpleLinkedListSet<String> simpleSet = new SimpleLinkedListSet<>();

        // Act
        boolean add1 = simpleSet.add(item1);
        boolean add2 = simpleSet.add(item1);

        assertTrue(add1);
        assertFalse(add2);
    }

    @Test
    void removing_none_existing_item_fails() {
        // Arrange
        SimpleLinkedListSet<String> simpleSet = new SimpleLinkedListSet<>();

        // Act
        boolean add1 = simpleSet.add(item4);
        boolean removed1 = simpleSet.remove("NonExisting");

        // Assert
        assertTrue(add1);
        assertFalse(removed1);
    }
}