package diranieh.workDistribution.workStealing;

public interface WorkStealingDequeue {
    void pushBottom(Runnable runnable);
    Runnable popBottom();
    Runnable popTop();
    boolean isEmpty();
}
