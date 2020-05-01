package diranieh.workDistribution.workStealing;

import diranieh.utilities.ThreadNumberGenerator;

import java.util.Queue;
import java.util.Random;

/* Each thread has its own task queue and all queues are stored in an array shared by
all threads. Each thread attempts to dequeue the next task from its own queue and runs
it if available. The thread then decides whether it should balance its work queue with
other threads. To re-balance, the thread chooses a victim thread uniformly at random.
The thread locks both queues in thread ID order (to avoid deadlock). If the difference
in queue sizes exceeds a threshold, queue sizes are made even */
public class WorkSharingThread extends Thread {
    private static final int THRESHOLD = 128;
    private final Queue<Runnable>[] _queues;
    private final Random _random;
    private volatile boolean _stop = false;
    public WorkSharingThread(Queue<Runnable>[] queues) {
        _queues = queues;
        _random = new Random();
    }

    public void stopRunning() {
        _stop = true;
    }

    public void run() {
        int myThreadId = ThreadNumberGenerator.get();
        while (!_stop) {
            // Dequeue a task and run it if it's available
            Runnable task = _queues[myThreadId].remove();
            if (task != null) task.run();

            // Rebalancing is probabilistic based on the thread's queue size. large queue sizes
            // have a lower probability to resize
            int myQueueSize = _queues[myThreadId].size();
            if (_random.nextInt(myQueueSize+1) == myQueueSize) {
                // Each queue in _queues is managed by a thread. Choose one such random thread
                int victimThreadId = _random.nextInt(_queues.length);

                // Lock queues in order of thread id
                int lowId = Math.min(victimThreadId, myThreadId);
                int highId = Math.max(victimThreadId, myThreadId);

                synchronized (_queues[lowId]) {
                    synchronized (_queues[highId]) {
                        balance(_queues[lowId], _queues[highId]);
                    }
                }
            }
        }
    }
    private void balance(Queue<Runnable> q0, Queue<Runnable> q1) {
        Queue<Runnable> qMin = (q0.size() < q1.size()) ? q0 : q1;
        Queue<Runnable> qMax = (qMin == q0) ? q1 : q0;

        int sizeDifference = (qMax.size() - qMin.size()) / 2;
        if (sizeDifference > THRESHOLD)
            while (qMax.size() > qMin.size())
                qMin.offer(qMax.remove());
    }
}
