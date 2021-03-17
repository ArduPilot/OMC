package android.content;

import android.content.res.AssetManager;
import android.content.res.Resources;

import javax.tools.FileObject;
import java.io.File;
import java.nio.channels.AsynchronousServerSocketChannel;

public abstract class Context {
    protected final AssetManager assetManager;
    protected final ContentResolver contentResolver;
    protected final Resources resources = new Resources();

    protected final File externalFilesDir;

    /**
     *
     * @param assetManager
     * @param externalFilesDir where to store public files
     */
    public Context(AssetManager assetManager, File externalFilesDir) {
        this.assetManager = assetManager;
        this.externalFilesDir = externalFilesDir;
        this.contentResolver = new ContentResolver();
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public AssetManager getAssets() {
        return assetManager;
    }

    /** storage for app data */
    public File getExternalFilesDir(String type) {
        return externalFilesDir;
    }

    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    public Resources getResources() {
        return resources;
    }
}
