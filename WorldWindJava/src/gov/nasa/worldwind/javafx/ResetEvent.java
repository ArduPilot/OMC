package gov.nasa.worldwind.javafx;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

class ResetEvent
{
    private static final class Sync extends AbstractQueuedSynchronizer
    {
        @Override
        public int tryAcquireShared(int acquires)
        {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        public boolean tryReleaseShared(int releases)
        {
            while (true)
            {
                int state = getState();
                if (state == 0)
                {
                    return false;
                }

                int next = state - 1;
                if (compareAndSetState(state, next))
                {
                    return next == 0;
                }
            }
        }

        public void reset()
        {
            setState(1);
        }
    }

    private final Sync sync = new Sync();

    ResetEvent() {
        sync.reset();
    }

    void await() throws InterruptedException
    {
        sync.acquireSharedInterruptibly(1);
    }

    void reset()
    {
        sync.reset();
    }

    boolean await(long timeout, TimeUnit unit) throws InterruptedException
    {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    void set()
    {
        sync.releaseShared(1);
    }
}
