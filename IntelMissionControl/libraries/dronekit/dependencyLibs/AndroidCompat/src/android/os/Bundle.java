package android.os;

import java.util.HashMap;
import java.util.Set;

public class Bundle {
    public static final Parcelable.Creator<Bundle> CREATOR =
            new Parcelable.Creator<Bundle>() {
                @Override
                public Bundle createFromParcel(Parcel in) {
                    return in.readBundle();
                }

                @Override
                public Bundle[] newArray(int size) {
                    return new Bundle[size];
                }
            };

    final HashMap<String, Object> hashMap;


    public Bundle(int i) {
        hashMap = new HashMap<>(i);
    }

    public Bundle() {
        hashMap = new HashMap<>();
    }


    public void putParcelable(String key, Object value) {
        hashMap.put(key, value);
    }

    public void putString(String key, Object value) {
        hashMap.put(key, value);
    }

    public void putByteArray(String key, byte[] value) {
        hashMap.put(key, value);
    }

    public String getString(String key) {
        Object o = hashMap.get(key);
        return o instanceof String ? (String) o : null;
    }

    public byte[] getByteArray(String key) {
        Object o = hashMap.get(key);
        return o instanceof byte[] ? (byte[]) o : null;
    }

    public void putInt(String key, int value) {
        hashMap.put(key, value);
    }

    public int getInt(String key, int defaultVal) {
        Object t = hashMap.get(key);
        return t instanceof Integer ? (Integer) t : defaultVal;
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public Object get(String key) {
        return hashMap.get(key);
    }

    public Set<String> keySet() {
        return hashMap.keySet();
    }

    public String getString(String key, String defaultVal) {
        Object t = hashMap.get(key);
        return t instanceof String ? (String) t : defaultVal;
    }

    public void putLong(String key, long value) {
        hashMap.put(key, value);
    }

    public long getLong(String key, long defaultVal) {
        Object t = hashMap.get(key);
        return t instanceof Long ? (Long) t : defaultVal;
    }

    public void putFloat(String key, float value) {
        hashMap.put(key, value);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public float getFloat(String key) {
        return getFloat(key, 0.f);
    }

    public float getFloat(String key, float defaultVal) {
        Object t = hashMap.get(key);
        return t instanceof Float ? (Float) t : defaultVal;
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        Object t = hashMap.get(key);
        return t instanceof Boolean ? (Boolean) t : defaultVal;
    }

    public <T> T getParcelable(String key) {
        return (T) hashMap.get(key);
    }

    public void setClassLoader(ClassLoader classLoader) {

    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public void putDouble(String key, double value) {
        hashMap.put(key, value);
    }

    public Bundle getBundle(String key) {
        Object t = hashMap.get(key);
        return t instanceof Bundle ? (Bundle) t : null;
    }

    public void putBoolean(String key, boolean value) {
        hashMap.put(key, value);
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    private double getDouble(String key, double defaultValue) {
        Object t = hashMap.get(key);
        return t instanceof Double ? (Double) t : 0.0;

    }

    @Override
    public String toString() {
        return "Bundle[" + hashMap + "]";
    }

    public void putBundle(String key, Bundle value) {
        hashMap.put(key, value);
    }
}
