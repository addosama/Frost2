package pub.frost.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

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

    public static byte[] readAllBytes(InputStream stream) throws IOException {
        int capacity = 8192;
        byte[] buf = new byte[capacity];
        int nread = 0;

        int n;

        while ((n = stream.read(buf, nread, capacity - nread)) > 0) {
            nread += n;

            if (nread == capacity) {
                capacity <<= 1;
                buf = Arrays.copyOf(buf, capacity);
            }
        }

        return Arrays.copyOf(buf, nread);
    }
}
