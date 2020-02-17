package diranieh.concurrentStacks;

class ConcurrentLockFreeStackWithEliminationTest implements SequentialStackTest, ConcurrentStackTest {
    @Override
    public Stack<Integer> createStack() {
        return new ConcurrentLockFreeStackWithElimination<>(10);        // elimination capacity
    }
}