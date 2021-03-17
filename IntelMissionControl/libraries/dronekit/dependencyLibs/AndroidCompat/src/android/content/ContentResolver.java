package android.content;

import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ContentResolver {

    public OutputStream openOutputStream(Uri uri) throws FileNotFoundException {
        Path path = Paths.get(uri.getUri());
        try {
            return Files.newOutputStream(path);
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException)e;
            }
        }
        return null;
    }

    public InputStream openInputStream(Uri uri) throws FileNotFoundException {
        Path path = Paths.get(uri.getUri());
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            if (e instanceof FileNotFoundException) {
                throw (FileNotFoundException)e;
            }
        }
        return null;
    }
}
