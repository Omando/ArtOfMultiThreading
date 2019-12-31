package diranieh.blockingsync.readerwriterlocks;

import java.util.concurrent.locks.Lock;

public interface ReaderWriterLock {
    Lock getReaderLock();
    Lock gerWriterLock();
}
