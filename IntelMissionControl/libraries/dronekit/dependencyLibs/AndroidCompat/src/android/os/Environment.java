package android.os;

import java.io.File;

public class Environment {
    private static File externalStorageDirectory;

    static {
        externalStorageDirectory = new File(".");
    }

    public static void setExternalStorageDirectory(File directory) {
        externalStorageDirectory = directory;
    }

    public static File getExternalStorageDirectory() {
        return externalStorageDirectory;
    }
}
