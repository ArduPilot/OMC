package androidCompat;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import androidCompat.content.DesktopAssetManager;

import java.io.File;

public class DesktopHelper {

    /**
     *
     * @param clazz used for loading assets
     * @param storage where External Storage Directory
     * @return
     */
    public static Context setup(Class<?> clazz, File storage) {
        if (clazz == null || storage == null) throw new IllegalArgumentException("clazz, storage must not be null");
        File appStorage = storage;
        File appPublicStorage = storage;

        Environment.setExternalStorageDirectory(appStorage);

        AssetManager assetManager = new DesktopAssetManager(clazz);
        Context context = new Context(assetManager, appPublicStorage) {};

        return context;
    }
}
