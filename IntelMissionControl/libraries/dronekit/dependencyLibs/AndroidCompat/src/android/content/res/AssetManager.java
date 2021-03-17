package android.content.res;

import java.io.IOException;
import java.io.InputStream;

public abstract class AssetManager {

    public abstract String[] list(String directory) throws IOException;
    public abstract InputStream open(String path);
}
