package android.net;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.net.URI;
import java.util.Objects;

public class Uri implements Parcelable {
    private final java.net.URI uri;

    /** not in android API */
    public URI getUri() {
        return uri;
    }

    /** not in android API */
    public Uri(URI uri) {
        this.uri = URI.create(uri.toString());
    }

    public static Uri fromFile(File file) {
        if (file == null) {
            throw new NullPointerException("file");
        }

        return new Uri(file.toURI());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uri uri1 = (Uri) o;
        return Objects.equals(uri, uri1.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Uri{" +
                "uri=" + uri +
                '}';
    }
}
