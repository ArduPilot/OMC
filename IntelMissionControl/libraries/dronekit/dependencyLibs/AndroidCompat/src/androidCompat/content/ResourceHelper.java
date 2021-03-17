package androidCompat.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceHelper {


    /**
     * enumerates all files in resource directory
     *
     * @param clazz
     * @param path path
     * @return
     * @throws IOException
     */
    public static List<String> getResourceFiles(Class<?> clazz, String path) throws IOException {
        List<String> resources = new ArrayList<>();


        try (InputStream is = getResourceAsStream(clazz, path);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String res;
            while ((res = br.readLine()) != null) {
                resources.add(res);
            }
        } catch (NullPointerException e) {

        }

        return resources;
    }

    public static InputStream getResourceAsStream(Class<?> clazz, String resource) {
        final InputStream in = clazz.getClassLoader().getResourceAsStream(resource);
        return in == null ? clazz.getResourceAsStream(resource) : in;
    }

}
