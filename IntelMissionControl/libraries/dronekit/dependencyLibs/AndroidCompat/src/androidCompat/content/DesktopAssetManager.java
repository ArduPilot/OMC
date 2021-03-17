package androidCompat.content;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class DesktopAssetManager extends AssetManager {
    final Class<?> clazz;

    /** read resources from this class */
    public DesktopAssetManager(Class<?> clazz) {
        this.clazz = clazz;
    }

    /** make path absolute, e.g. "foo/bar" -> "/foo/bar" */
    private static String addPrecedingSlash(String directory) {
        return directory.startsWith("/") ? directory : "/" + directory;
    }

    public String[] list(String directory) throws IOException {
        if (directory == null) throw new IllegalArgumentException("path is null");

        List<String> resourceFiles = ResourceHelper.getResourceFiles(clazz, addPrecedingSlash(directory));
        String[] resArr = new String[resourceFiles.size()];
        return resourceFiles.toArray(resArr);
    }

    public InputStream open(String path) {
        if (path == null) throw new IllegalArgumentException("path is null");

        return ResourceHelper.getResourceAsStream(clazz, addPrecedingSlash(path));
    }
}
