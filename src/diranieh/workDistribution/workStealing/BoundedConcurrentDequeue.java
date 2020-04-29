package diranieh.workDistribution.workStealing;

/*
 Bounded concurrent dequeue. WorkStealingThread class maintains an array
 of these dequeues with a dequeue for each thread.

 This class is the concurrent version of the thread-unsafe
 BoundedSequentialDequeue class. Look at BoundedSequentialDequeue first
 before looking at this code.
 */

import java.util.concurrent.atomic.AtomicStampedReference;

public class BoundedConcurrentDequeue {
    private Runnable[] tasks;           // holds tasks in the queue
    private volatile int bottom;        // Index of the first EMPTY slot in tasks array

    // Recall that an AtomicStampedReference maintains an object reference that is the index
    // of the first task in the tasks queue, and an integer stamp that acts as a counter that
    // gets incremented each time the reference is changed (the stamp is needed to avoid the
    // ABA problem when using compareAndSet)
    private AtomicStampedReference<Integer> top;    // index of the first task in the tasks array

    public BoundedConcurrentDequeue(int capacity) {
        tasks = new Runnable[capacity];
        top = new AtomicStampedReference<>(0,0);
        bottom = 0;
    }

    // A thread attempting to steal a task from another thread, will get the thread's dequeue
    // (i.e., an instance of this class) and calls isEmpty to determine if there are tasks
    // to steal
    public boolean isEmpty() {
        int localTop = top.getReference();
        return bottom <= localTop;
    }

    public void pushBottom(Runnable task) {
        tasks[bottom] = task;
        bottom++;
    }

    public Runnable popTop() {
        /*Logic for sequential bounded dequeue:
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");
        E item = _items[_indexTop];
        _indexTop++;
        return item;*/

        if (isEmpty())
            return null;

        // This block is standard whenever we want to set a new value for top
        int[] stamp = new int[1];
        int oldTop = top.get(stamp);
        int newTop = oldTop + 1;
        int oldStamp = stamp[0];
        int newStamp = oldStamp + 1;

        // Try to steal the top element
        Runnable runnable = tasks[oldTop];
        if (top.compareAndSet(oldTop, newTop, oldStamp, newStamp))
            return runnable;

        // Theft unsuccessful. Returning null does not mean that the dequeue is empty
        return null;
    }

    public Runnable popBottom() {
        /*Logic for sequential bounded dequeue:
        if (isEmpty())
            throw new IllegalStateException("Dequeue is empty");

        E item = _items[_indexBottom - 1];
        _indexBottom--;
        return item; */

        if (isEmpty())
            return null;

        // Claim a task. If the claimed task was last in the queue, it is important that thieves
        // notice that the dequeue is empty. This is why bottom was declared volatile; it ensures
        // that thieves will read the most up-to-date value of bottom (recall that volatile fields
        // always guarantee visibility (but not atomicity)
        bottom--;   // recall: volatile
        Runnable task = tasks[bottom];

        // Test whether the current top field refer to a higher index. If so, the caller cannot
        // conflict with a thief and the method returns
        int[] stamp = new int[1];
        int oldTop = top.get(stamp);
        int newTop = 0;
        int oldStamp = stamp[0];
        int newStamp = oldStamp + 1;
        if (bottom > oldTop)
            return task;

        // If the top and bottom fields are equal, then there is only one task left. The caller
        // may conflict with a thief. The caller resets bottom to zero as either the caller will
        // succeed in claiming the task or a thief will steal it first. The caller resolves the
        // conflict by calling CAS to set top to 0, matching bottom
        if (bottom == oldTop) {
            bottom = 0;
            if (top.compareAndSet(oldTop, newTop, oldStamp, newStamp ))
                return task;        // task claimed by caller
        }

        // The queue must be empty because a thief succeeded. This means that top points to some
        // entry greater than bottom which was set to 0 earlier. So reset top to zero and return
        top.set(newTop, newStamp);
        bottom = 0;
        return null;
    }
}



