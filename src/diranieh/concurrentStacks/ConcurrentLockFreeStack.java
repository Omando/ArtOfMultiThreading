package diranieh.concurrentStacks;

public class ConcurrentLockFreeStack<E> implements Stack<E> {
    private static class Node<E> {
        private E item;
        private Node<E> next;

        public Node(E item) {
            this(item, null);
        }

        public Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    // Sentinels
    private Node<E> head;
    private Node<E> tail;

    public ConcurrentLockFreeStack() {
            head =  new Node<>(null);
            tail = head;
    }

    @Override
    public void push(E item) {
        // Create a new node
        Node<E> newNode = new Node<>(item);

        // Keep on retrying until successful
        while (true) {
            // Add CAS logic here
        }
    }

    @Override
    public E pop() {

        // TODO
        return null;
    }
}
