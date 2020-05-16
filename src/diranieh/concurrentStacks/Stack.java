package diranieh.concurrentStacks;

/**
 * The {@code Stack} class represents a last-in-first-out (LIFO) stack of objects
 * @param <E> the type of elements in the stack
 */
public interface Stack<E> {
    void push(E item);
    E pop();
    boolean isEmpty();
}
