package diranieh.concurrentQueues;

import java.util.concurrent.atomic.AtomicReference;

public class SynchronousDualQueue<E> implements Queue<E> {
    // To support calling CompareAndSet on next, declare it as AtomicReference<Node<E>>
    private enum NodeType {ITEM, RESERVATION};
    private static class Node<E> {
        private NodeType type;
        private AtomicReference<E> item;
        private AtomicReference<Node<E>> next;

        public Node(E item, NodeType type) {
            this(item, type, null);
        }

        public Node(E item, NodeType type, Node<E> next) {
            this.item = new AtomicReference<>(item);
            this.type = type;
            this.next = new AtomicReference<>(next);
        }

        public String toString() {
            return String.format("Node[Type = %1s, Item = %2s, Next = %3s]", type, item, next);
        }
    }

    private AtomicReference<Node<E>> head;
    private AtomicReference<Node<E>> tail;

    public SynchronousDualQueue() {
        // Create a sentinel node whose value is meaningless and points to
        // nothing (next = null). Initially, both head and tail point to
        // the sentinel
        Node<E> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail =  new AtomicReference<>(sentinel);
    }

    @Override
    public void enqueue(E element) throws InterruptedException {
        Node<E> offer = new Node<>(element, NodeType.ITEM)

    }

    @Override
    public E dequeue() throws InterruptedException {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
