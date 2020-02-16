package diranieh.concurrentStacks;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An array of Exchanger objects with a maximum given capacity
 *
 * We want two threads, one pushing and one popping, to coordinate and cancel out,
 * but must avoid a situation in which a thread coordinates and cancels out more
 * than one thread. This is done by implementing the EliminationArray using
 * coordination structures called exchangers; objects that allow exactly two threads
 * (and no more) to rendezvous and exchange values.
 *
 * A thread attempting to exchange picks an array entry at random and calls the
 * entry's exchange method, providing its own value to exchange.
 * @param <E> the type of elements in this array
 */
public class EliminationArray<E> {
    private static final int duration = 10;
    private static final TimeUnit unit = TimeUnit.MILLISECONDS;
    private EliminationExchanger<E>[] exchanger;
    private final Random random;

    public EliminationArray(int capacity) {
        // Allocate exchanger array and initialize each entry
        exchanger = (EliminationExchanger<E>[]) new EliminationExchanger[capacity];
        for(int i = 0; i < capacity; i++) {
            exchanger[i] = new EliminationExchanger<>();
        }

        random = new Random( new Date().getTime());
    }

    // Given a value of type E, either return the value input by the exchange partner,
    // or throw an exception if the timeout expires without exchanging a value with
    // another thread
    // Parameter bound is used as an upper bound on the slot index and is determined
    // dynamically based on the load on the data structure
    public E visit(E mine, int bound) throws TimeoutException {
        // Return a random slot index such that the index is in the range [0, bound)
        int slotIndex = random.nextInt(bound);

        return exchanger[slotIndex].exchange(mine, duration, unit);
    }
}
