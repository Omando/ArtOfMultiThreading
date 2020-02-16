package diranieh.concurrentStacks;

/**
 * RangePolicy determines the EliminationArray subrange to be used.
 *
 * RangePolicy records both successful exchanges and timeout failures.
 * Successful exchanges increase the range while timeouts reduce the
 * range.
 */
public class RangePolicy {
    private final int capacity;
    private int currentRange;

    public RangePolicy(int capacity) {
        this.capacity = capacity;
        currentRange = 1;
    }

    public void recordEliminationSuccess() {
        if (currentRange < capacity)
            ++currentRange;
    }

    public void recordEliminationTimeout() {
        if (currentRange > 1)
            --currentRange;
    }

    public int getRange() {
        return currentRange;
    }
}
