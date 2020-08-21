package dataentities.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Handles locks by allowing the lock to automatically unlock */
public class LockHandler implements AutoCloseable {

    private final Lock lock;

    LockHandler(Lock lock) {
        lock.lock();
        this.lock = lock;
    }

    /** Gets a read lock */
    public static LockHandler ReadMode(ReentrantReadWriteLock lock) {
        return new LockHandler(lock.readLock());
    }

    /** Get a write lock */
    public static LockHandler WriteMode(ReentrantReadWriteLock lock) {
        return new LockHandler(lock.writeLock());
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
