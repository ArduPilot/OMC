package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;

public class CreateDatasetRequest extends MRequest {
    static long slowest = 0;
    static long noSampled = 0;
    private final String name;

    public CreateDatasetRequest(String name) {
        super(100, 3);
        this.name = name;
    }

    @Override
    public String toString() {
        return "Create dataset profile request" + ": " + name;
    }

    @Override
    public synchronized boolean isSlowestUpToNow(long duration) {
        if (duration > slowest) {
            slowest = duration;
            return true;
        }

        return false;
    }

    public synchronized void sampleThis() {
        noSampled++;
    }

    @Override
    public long getCountUpToNow() {
        return noSampled;
    }
}
