package diranieh.utilities;

/**
 * Assign sequential Ids to threads
 * If thread A is the 1st thread to call get(), it will 0 (nextId then POST incremented to 1)
 * If thread B is the 2nd thread to call get(), it will 1 (nextId then POST incremented to 2)
 * If thread C is the 3re thread to call get(), it will 2 (nextId then POST incremented to 3)
 * and so on
 */
public class ThreadNumberGenerator {
    private static int nextId = 0;

    // Always helps to think of ThreadLocal<T> as a Map<Thread, T>
    // that stores thread-specific values
    private static ThreadLocal<Integer> threadNumber = ThreadLocal.withInitial(() -> nextId++);

    public static int get() {
        return threadNumber.get();
    }

    public static void reset() {
        threadNumber.set(0);
    }
}
