package diranieh.concurrentStacks;

class ConcurrentLockFreeStackTest implements  SequentialStackTest, ConcurrentStackTest {

    @Override
    public Stack<Integer> createStack() {
        return new ConcurrentLockFreeStack<Integer>();
    }
}