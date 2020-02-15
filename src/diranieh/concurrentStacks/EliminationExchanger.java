package diranieh.concurrentStacks;


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
}
