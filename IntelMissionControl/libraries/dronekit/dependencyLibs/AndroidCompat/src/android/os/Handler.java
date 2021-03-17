package android.os;

import java.util.LinkedHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Handler {
    final Looper looper;
    final LinkedHashMap<Runnable, HandlerTask> taskMap = new LinkedHashMap<>();

    public Handler(Looper looper) {
        this.looper = looper;
    }


    public Handler() {
        this.looper = new Looper();
//        throw new UnsupportedOperationException();
    }

    public Looper getLooper() {
        return looper;
    }

    class HandlerTask implements Runnable {
        final Runnable delegate;
        public Future<?> future;

        HandlerTask(Runnable delegate) {
            this.delegate = delegate;
            taskMap.put(delegate, this);
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } finally {
                taskMap.remove(delegate);
            }
        }
    }

    public void post(Runnable runnable) {
        HandlerTask task = new HandlerTask(runnable);
        task.future = looper.executorService.submit(task);
    }

    public void removeCallbacks(Runnable runnable) {
        HandlerTask remove = taskMap.remove(runnable);
        if (remove == null) return;

        boolean cancel = remove.future.cancel(false);
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        HandlerTask task = new HandlerTask(runnable);
        task.future = looper.executorService.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }
}
