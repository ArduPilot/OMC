package android.os;

import java.util.Collection;
import java.util.List;

public class Parcel {

    public static Parcel obtain() {
        return new Parcel();
    }

    public void writeInt(int messageType) {
    }

    public void writeParcelable(Parcelable coordinate, int flags) {
    }


    public int readInt() {
        return 0;
    }

    public Object readSerializable() {
        return null;
    }

    public void writeTypedList(Object data) {

    }


    public final <T extends Parcelable> T readParcelable(ClassLoader loader) {
        return null;
    }

    public final <T> void readTypedList(List<T> list, Parcelable.Creator<T> c) {

    }

    public void writeSerializable(Object objs) {
    }

    public void writeString(String mStatusCode) {
    }

    public void writeBundle(Bundle mExtras) {

    }

    public String readString() {
        return null;
    }

    public Bundle readBundle() {
        return null;
    }

    public void writeLong(long eventsDispatchingPeriod) {

    }

    public long readLong() {
        return 0;
    }

    public Bundle readBundle(ClassLoader classLoader) {
        return null;
    }

    public void writeDouble(double speed) {
    }

    public double readDouble() {
        return 0;
    }

    public void writeByte(byte b) {
    }

    public byte readByte() {
        return 0;
    }

    public void writeFloat(float data) {
    }

    public float readFloat() {
        return 0;
    }

    public void writeValue(Object batteryDischarge) {
    }

    public <T> T readValue(ClassLoader classLoader) {
        return null;
    }

    public byte[] marshall() {
        return new byte[0];
    }

    public void recycle() {
        
    }

    public void setDataPosition(int i) {
    }

    public void unmarshall(byte[] bytes, int i, int length) {
    }
}