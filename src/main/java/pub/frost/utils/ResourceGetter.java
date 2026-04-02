package pub.frost.utils;

import java.io.InputStream;
import java.net.URL;

public class ResourceGetter {
    public static URL get(ClassLoader loader, String path) {
        return loader.getResource(path);
    }
    public static InputStream getAsStream(ClassLoader loader, String path) {
        return loader.getResourceAsStream(path);
    }

    public static String getClientResourcePath() {
        return "assets/frost/";
    }
    public static URL getClientResource(String path) {
        return get(ResourceGetter.class.getClassLoader(), getClientResourcePath() + path);
    }
    public static InputStream getClientResourceAsStream(String path) {
        return getAsStream(ResourceGetter.class.getClassLoader(), getClientResourcePath() + path);
    }
}
