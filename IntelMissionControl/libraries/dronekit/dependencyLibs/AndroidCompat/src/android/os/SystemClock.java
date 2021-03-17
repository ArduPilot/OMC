package android.os;

public class SystemClock {

    /** Returns milliseconds since boot, including time spent in sleep. */
    public static long elapsedRealtime() {
        return System.currentTimeMillis();
    }
}
