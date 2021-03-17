package android.location;

public abstract class Location {
    public static final Object FORMAT_DEGREES = 1;
    public static final Object FORMAT_SECONDS = 2;

    public static String convert(double latitude, Object formatDegrees) {
        return null;
    }

    public static void distanceBetween(double latitude, double longitude, double latitude1, double longitude1, float[] results) {
        throw new UnsupportedOperationException("not implemented");
    }

    public double getLatitude() {
        return 0.0;
    }

    public double getLongitude() {
        return 0.0;
    }

    public double getAltitude() {
        return 0;
    }

    public float getBearing() {
        return 0;
    }

    public float getSpeed() {
        return 0;
    }

    public long getTime() {
        return 0L;
    }

    public boolean hasBearing() {
        return false;
    }

    public void setBearing(float headingFromCoordinates) {
    }

    public boolean hasAccuracy() {
        return false;
    }

    public float distanceTo(Location mLastLocation) {
        return 0;
    }

    public float getAccuracy() {
        return 0;
    }
}
