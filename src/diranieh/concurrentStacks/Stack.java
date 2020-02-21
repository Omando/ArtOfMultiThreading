package diranieh.concurrentStacks;

public interface Stack<E> {
    void push(E item);
    E pop();
    boolean isEmpty();
}