package android.os;

public interface Parcelable {
    int describeContents();

    void writeToParcel(Parcel dest, int flags);

    interface Creator<T> {
        public T createFromParcel(Parcel source);
        public T[] newArray(int size);
    }
}
