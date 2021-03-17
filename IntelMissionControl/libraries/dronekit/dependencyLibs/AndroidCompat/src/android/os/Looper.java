package android.os;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Looper {
    ScheduledExecutorService executorService;

    public Looper() {
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void quitSafely() {
        executorService.shutdown();
    }

    public void awaitShutdown() {
        quitSafely();
        try {
            executorService.awaitTermination(4, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Looper getMainLooper() {
        return LooperHolder.INSTANCE;
    }

    /** initialization-on-demand holder */
    private static class LooperHolder {
        static final Looper INSTANCE = new Looper();
    }
}
