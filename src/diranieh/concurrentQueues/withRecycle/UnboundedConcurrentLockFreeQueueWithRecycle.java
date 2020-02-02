package diranieh.concurrentQueues.withRecycle;

import diranieh.concurrentQueues.Queue;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * An unbounded non-blocking concurrent queue implemented using a linked list with
 * dynamic memory allocation
 *
 * The ABA problem often shows up in lock-free algorithms that use CAS and dynamic memory
 * allocations. One way to fix this problem it to tag each node with a unique stamp. This
 * can be easily done with AtomicStampedReference<T> as shown in the declaration of Node<E>
 *
 * @param <E> the type of elements in this list
 */
public class UnboundedConcurrentLockFreeQueueWithRecycle<E> implements Queue<E> {
    private final NodeManager<E> nodeManager = new NodeManager<>();

    // Sentinels
    private AtomicStampedReference<RecyclableNode<E>> head;
    private AtomicStampedReference<RecyclableNode<E>> tail;

    public UnboundedConcurrentLockFreeQueueWithRecycle() {
        // Create a sentinel node whose value is meaningless and points to nothing
        // (next = null). Initially, both head and tail point to the sentinel
        RecyclableNode<E> sentinel = new RecyclableNode<>(null);
        head = new AtomicStampedReference<>(sentinel, 0);
        tail =  new AtomicStampedReference<>(sentinel, 0);
    }

    @Override
    public void enqueue(E element)  {
        // Create a new node with the value to be enqueued
        RecyclableNode<E> newNode = nodeManager.allocateNode(element);
        int[] lastStamp = new int[1];
        int[] nextStamp = new int[1];
        int[] stamp = new int[2];

        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while(true) {
            /* After each result is obtained, another thread my make changes*/
            // Locate the last node in the queue
            RecyclableNode<E> last = tail.get(lastStamp);
            RecyclableNode<E> next = last.next.get(nextStamp);

            if (last == tail.get(stamp) && lastStamp[0] == stamp[0]) {       // Do we have the tail?
                if (next == null) {         // No other threads enqueue an item (but not yet advanced tail)?
                    // So far, last is actually the tail since last.next is null. Attempt to
                    // set last.next to the new node, then move (or advance) tail to the new node
                    if (last.next.compareAndSet(next, newNode, nextStamp[0], nextStamp[0]+1)) {
                        tail.compareAndSet(last, newNode, lastStamp[0], lastStamp[0] + 1);
                        return;
                    }
                } else {
                    // last is not the tail since last.next is now not null
                    // So move tail to next
                    tail.compareAndSet(last, next, lastStamp[0], lastStamp[0] + 1);
                }
            }
        }
    }

    @Override
    public E dequeue() {
        int[] lastStamp = new int[1];
        int[] firstStamp = new int[1];
        int[] nextStamp = new int[1];
        int[] stamp = new int[1];

        // A CAS requires a while(true) statement to keep on trying while CAS fails
        while (true) {
            RecyclableNode<E> first = head.get(firstStamp);             // head points to a sentinel with no meaningful value
            RecyclableNode<E> successor = first.next.get(nextStamp);    // actual head is pointed to by the sentinal
            RecyclableNode<E> last = tail.get(lastStamp);               // tail node. See document on why this is needed

            // The next three if statements check if the queue is empty
            if (first == head.get(stamp) && stamp[0] == firstStamp[0]) {
                if (first == last) {
                    if (successor == null)
                        throw new IllegalStateException("Queue is empty");

                    // tail is behind, try to advance (see document for full explanation)
                    tail.compareAndSet(last, successor, lastStamp[0], lastStamp[0] + 1);
                } else {
                    // Read value of successor
                    E value = successor.item;

                    // Since we have captured the value of teh successor, We 'dequeue' the
                    // successor by making it the sentinel node (recall that we do not care
                    // about the value of the sentinel)
                    if (head.compareAndSet(first, successor, firstStamp[0], firstStamp[0] + 1))
                        return value;
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        int[] lastStamp = new int[1];
        int[] firstStamp = new int[1];
        int[] nextStamp = new int[1];
        int[] stamp = new int[1];

        RecyclableNode<E> first = head.get(firstStamp);
        RecyclableNode<E> last = tail.get(lastStamp);
        RecyclableNode<E> next = first.next.get(nextStamp);
        return (first == head.get(stamp) && stamp[0] == firstStamp[0] &&
                first == last &&
                next == null);
    }
}
