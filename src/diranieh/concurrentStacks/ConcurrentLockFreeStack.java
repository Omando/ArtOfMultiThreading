package diranieh.concurrentStacks;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLockFreeStack<E> implements Stack<E> {
    private static class Node<E> {
        private E item;
        private AtomicReference<Node<E>> next;

        public Node(E item) {
            this(item, null);
        }

        public Node(E item, Node<E> next) {
            this.item = item;
            this.next = new AtomicReference<>(next);
        }
    }

    // Sentinels
    private Node<E> head;

    public ConcurrentLockFreeStack() {
            head =  new Node<>(null);
    }

    @Override
    public void push(E item) {
        // Create a new node
        Node<E> newNode = new Node<>(item);

        // Keep on retrying until successful
        while (true) {
            // Get current item at head
            Node<E> top = head.next.get();
            newNode.next.set(top);
            if (head.next.compareAndSet(top, newNode))
                return;
        }
    }

    @Override
    public E pop() {
        while (true) {
            Node<E> top = head.next.get();
            if (top == null)
                return null;

            Node<E> next = top.next.get();
            if (head.next.compareAndSet(top, next))
                return top.item;
        }
    }

    @Override
    public boolean isEmpty() {
        return head.next.get() == null;
    }
}
