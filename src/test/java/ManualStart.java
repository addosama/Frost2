import net.minecraft.client.main.Main;

import java.util.Arrays;

public class ManualStart {
    public static void main(String[] args) {
        if (!System.getProperty("user.dir").endsWith("run")) return;
        Main.main(concat(new String[]{"--version", "Forge189", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
