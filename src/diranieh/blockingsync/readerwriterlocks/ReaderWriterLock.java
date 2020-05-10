package diranieh.blockingsync.readerwriterlocks;

import java.util.concurrent.locks.Lock;

/* Implemented by {@link FairReaderWriterLock} and {@link SimpleReaderWriterLock} */
public interface ReaderWriterLock {
    Lock getReaderLock();
    Lock gerWriterLock();
}
