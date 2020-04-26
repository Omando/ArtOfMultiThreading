package diranieh.workDistribution.workStealing;

/*
 Bounded concurrent dequeue
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
}



