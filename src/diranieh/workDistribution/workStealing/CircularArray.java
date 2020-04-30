package diranieh.workDistribution.workStealing;

public class CircularArray {
    private int _logCapacity;
    private Runnable[] _currentTasks;

    // logCapacity to capacity:
    //  1: 1 << 1 =   10b = 2
    //  2: 1 << 2 =  100b = 4,
    //  3: 1 << 3 = 1000b = 8
    // etc
    public CircularArray(int logCapacity) {
        _logCapacity = logCapacity;
        _currentTasks = new Runnable[1 << logCapacity];
    }

    int capacity() {
        return 1 << _logCapacity;
    }

    // Modulus operator is cycle
    Runnable get(int index) {
        return _currentTasks[ index % capacity()];
    }

    void put(int index, Runnable task) {
        _currentTasks[ index % capacity()] = task;
    }

    // Create a new circular array and populate with items from this circular
    // array whose index fall between bottom inclusive and top exclusive
    CircularArray resize(int bottom, int top) {
        // Increasing log by 1 means multiplying by 2
        CircularArray newArray = new CircularArray(_logCapacity + 1);

        // Copy items whose indices is between top and bottom to the new
        // circular array
        for (int i = top; i < bottom; i++) {
            newArray.put(i, get(i));
        }
        return  newArray;
    }
}
