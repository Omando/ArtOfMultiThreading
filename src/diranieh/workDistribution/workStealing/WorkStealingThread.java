package diranieh.workDistribution.workStealing;

import diranieh.utilities.ThreadNumberGenerator;

import java.util.Random;

/* A work-stealing thread that maintains a pool of tasks to be executed.
 All threads share an array of Dequeue which each array entry representing
 a pool of tasks specific to a thread id.
 On construction, an array of dequeues is passed in representing all
 available pool. If this thread has no more tasks in its pool, it will
 choose a random thread and steal the first available work item (by calling
 popTop method on the thread's pool
*/
public class WorkStealingThread {
    private final WorkStealingDequeue[] _dequeues;
    private final Random _random;

    public WorkStealingThread(WorkStealingDequeue[] dequeues) {
        _dequeues = dequeues;
        _random = new Random();
    }

    public void run() {
        // Get the sequential id of this thread. This id identifies the
        // index of the array cell where this thread's pool is stored
        int indexThisThread = ThreadNumberGenerator.get();

        // Loop for ever
        Runnable runnable;

        // TODO: while(true): Missing termination. Use Barrier
        while (true) {
            // If I have a task in my pool, then run it
            runnable = _dequeues[indexThisThread].popBottom();
            if (runnable != null)
                runnable.run();
            else {
                // This thread has no more tasks available. Ensure that other threads
                // that have work to do are not unreasonably delayed by thief threads
                // which are idle except for task stealing. To prevent this situation
                // call Thread.yield before stealing a task to let the scheduler know
                // that this thread is willing to yield its current use of a processor
                Thread.yield();

                // There is no task available. Choose a random thread and try
                // to steal a task
                int victim = _random.nextInt(_dequeues.length);
                runnable = _dequeues[victim].popTop();
                if (runnable != null) runnable.run();
            }
        }
    }
}
