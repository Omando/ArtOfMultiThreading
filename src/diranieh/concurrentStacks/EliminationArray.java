package diranieh.concurrentStacks;

/**
 *
 * We want two threads, one pushing and one popping, to coordinate and cancel out,
 * but must avoid a situation in which a thread coordinates and cancels out more
 * than one thread. This is done by implementing the EliminationArray using
 * coordination structures called exchangers, objects that allow exactly two threads
 * (and no more) to rendezvous and exchange values
 *
 * @param <E> the type of elements in this array
 */
public class EliminationArray<E> {
}
