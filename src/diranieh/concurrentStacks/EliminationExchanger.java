package diranieh.concurrentStacks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

/** Permit two threads to exchange values of type E.
 *
 * If thread A calls exchange(a) and thread B calls exchange(b), then A's call
 * will return b and B's call wil return a.
 *
 * The exchanger works as follows: thread A calls exchange to write a value and
 * spins until a second thread arrives. The second thread, B, detects that thread
 * A waiting, so thread B reads thread A's value and signals the exchange. Each
 * thread reads the other's value and returns.
 * The first thread's call times out if the second thread does not call, allowing
 * the first thread to leave the exchanger if it does not exchange a value in a
 * reasonable time.
 *
 * @param <E> the type of elements in this array
 */

public class EliminationExchanger<E> {
    private static final int EMPTY = 0;     // Slot is empty
    private static final int WAITING = 1;   // A thread is waiting for rendez-vous with another thread at affected slot
    private static final int BUSY = 2;      // Other threads busy with rendez-vous at affected slot
    private AtomicStampedReference<E> slot = new AtomicStampedReference<>(null, 0);

    /* A thread attempts to exchange values by reading the state of the slot and
    proceeding as follows:
    EMPTY    : Use CAS to try to place an item in the slot and set slot state to WAITING;
              If CAS fails retry until success or timeout. If CAS succeeds, spin and wait
              for another thread to complete the exchange. While spinning, another thread
              may or may not show up:
              If another thread shows up and finds the state is WAITING, so it takes the
              item in the slot and replaces it with its own and sets the state to BUSY
              indicating to the waiting thread that the exchange is complete. The waiting
              thread then consumes the item and sets the state to EMPTY.
              If another thread does not show up, the waiting thread must reset the state
              of the slot using CAS to EMPTY, and if successful throws a timeout exception
              But if CAS fails, then another thread has shown up and the waiting thread in
              this case completes.
    WAITING  : Another thread is waiting and the slot has an item. This thread takes the
              item in the slot and replaces it with its own and sets the state to BUSY
              indicating to the waiting thread that the exchange is complete. It may fail
              if another thread succeeds, or the other thread resets the state to EMPTY
              following a timeout. If so, the thread must retry. If it does succeed changing
              the state to BUSY, then it can return the item.
    BUSY     : Two other threads are currently using the slot for an exchange; the thread
              must retry
     */
    public E exchange(E myItem, long timeout, TimeUnit unit) throws TimeoutException {
        // Get system time when the timeout occurs
        long timeLimit = GetTimeLimitFromTimeout(timeout, unit);

        // Loop until a value is exchanged or the timeout has expired
        int[] stampHolder = {EMPTY};
        while (true) {
            // Timed out?
            if (System.nanoTime() > timeLimit) throw new TimeoutException();

            // Get existing item and the state of the slot as indicated by stampHolder
            E yourItem = slot.get(stampHolder);
            int stamp = stampHolder[0];

            // Check state of the slot. Exchange slot has three states: EMPTY, WAITING and BUSY
            switch (stamp) {
                case EMPTY:
                    // Try to place an item in the slot and set slot state to WAITING;
                    if (slot.compareAndSet(yourItem, myItem, EMPTY, WAITING)) {
                        // CAS success: spin and wait for another thread to complete the exchange
                        // by setting the state of the slot to BUSY
                        while (System.nanoTime() < timeLimit) {
                            // Check the state of the slot. If BUSY, then another thread has
                            // exchanged.
                            yourItem = slot.get(stampHolder);
                            if (stampHolder[0] == BUSY) {
                                // Resetting to EMPTY is done using a simple write because the waiting
                                // thread is the only one that can change the state from BUSY to EMPTY
                                slot.set(null, EMPTY);
                                return yourItem;
                            }

                            // So far, no thread showed up. Continue spinning and waiting for another
                            // thread to  show up and complete the exchange until the timeout expires
                        }   // while

                        // Reaching here means that no thread showed up to complete the exchange.
                        // The waiting thread must now reset the state of the slot using CAS to EMPTY.
                        // Resetting the state to EMPTY requires CAS because other threads might be
                        // attempting  to exchange by setting the state from WAITING to BUSY
                        if (slot.compareAndSet(myItem, null, WAITING, EMPTY))
                            // Failed to exchange within timeout, so throw an exception
                            throw new TimeoutException("exchange timed out");
                        else {
                            // Another thread has shown up and the waiting thread can complete
                            yourItem = slot.get(stampHolder);
                            slot.set(null, EMPTY);
                            return yourItem;
                        }
                    }
                    // CAS to place an item in the slot and set slot to WAITING failed,
                    // so retry until success or timeout.
                    break;
                case WAITING:
                    if (slot.compareAndSet(yourItem, myItem, WAITING, BUSY))
                        return yourItem;
                    break;
                case BUSY:
                    // Two other threads are currently using the slot for an exchange. Retry
                    break;
                default:
                    throw new IllegalStateException("Unknown slot state: " + stamp);
            }
        }
    }

    // Get the corresponding system time for a timeout value
    private long GetTimeLimitFromTimeout(long timeout, TimeUnit unit) {
        // Given a timeout and a unit, convert it to nanos (for example, given
        //  a timeout of 1 and a unit of msec, unit.ToNanos(1) returns 10^6
        long timeoutInNanos = unit.toNanos(timeout);

        // Calculate time limit from NOW after which we timeout
        return System.nanoTime() + timeoutInNanos;
    }
}
