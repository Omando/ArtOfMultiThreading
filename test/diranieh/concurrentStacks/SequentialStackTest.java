package diranieh.concurrentStacks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface SequentialStackTest extends BaseStackTest<Integer> {
    final static int SAMPLE_SIZE = 512;

    @Test
    default void new_stack_should_be_empty() {
        // Arrange & Act
        Stack<Integer> stack = createStack();

        // Assert
        assertTrue(stack.isEmpty());
    }

    @Test
    default void should_pop_all_pushed_items() throws InterruptedException {
        // Arrange
        Stack<Integer> stack = createStack();

        // Act
        for(int i = 0; i < SAMPLE_SIZE; ++i) {
            stack.push(i);
        }
        for(int i = SAMPLE_SIZE - 1; i >= 0; --i) {
            int item = stack.pop();
            assertEquals(i, item);
        }
    }
}

