package diranieh.workDistribution.workStealing;

import java.util.concurrent.atomic.AtomicInteger;

public class UnboundedConcurrentDequeue {
    private CircularArray _tasks;  // holds tasks in the queue
    private volatile int _bottom;           // Index of the first EMPTY slot in tasks array
    private AtomicInteger _top;             // index of the first task in the tasks array

    public UnboundedConcurrentDequeue(int LOG_CAPACITY) {
        _tasks =  new CircularArray(LOG_CAPACITY);
        _top  = new AtomicInteger(0);
        _bottom = 0;
    }

    public boolean isEmpty() {
        return _bottom <= _top.get();
    }

    public void pushBottom(Runnable runnable) {
        int oldBottom = _bottom;
        int oldTop = _top.get();
        int size = oldBottom - oldTop;

        // Before pushing, have we reached capacity? If yes, resize
        if (size >= _tasks.capacity() - 1) {
             _tasks = _tasks.resize(oldBottom, oldTop);
        }

        _tasks.put(oldBottom, runnable);
        _bottom = oldBottom + 1;
    }

    public Runnable popTop() {
        int oldTop = _top.get();
        int newTop = oldTop + 1;
        int oldBottom = _bottom;            // important that top read before bottom
        CircularArray currentTasks = _tasks;
        int size = oldBottom - oldTop;
        if (size <= 0) return null;         // empty
        Runnable task = _tasks.get(oldTop);
        if (_top.compareAndSet(oldTop, newTop))
            return task;
        return null;
    }

    public Runnable popBottom() {
        CircularArray currentTasks = _tasks;
        _bottom--;
        int oldTop = _top.get();
        int newTop = oldTop + 1;
        int size = _bottom - oldTop;
        if (size < 0) {
            _bottom = oldTop;
            return null;
        }
        Runnable task = _tasks.get(_bottom);
        if (size > 0)
            return task;
        if (!_top.compareAndSet(oldTop, newTop))
            return null; // queue is empty
        _bottom = oldTop + 1;
        return task;
    }

}
