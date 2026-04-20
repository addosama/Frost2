package pub.frost.client.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import pub.frost.client.core.FrostCore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ConfigManager {
    private static final File CONFIG_DIR = FrostCore.getClientDir().resolve("config").toFile();
    private final Map<Path, Config> configMap = new HashMap<>();
    private Config currentConfig;

    public void refreshConfigList() {
        configMap.clear();
        checkConfigPath();
        File[] fileArray = CONFIG_DIR.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                try {
                    String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    if (!JSON.isValid(fileContent)) continue;
                    configMap.put(file.toPath().toRealPath(), new Config(file.getCanonicalFile(), JSON.parseObject(fileContent)));
                } catch (IOException ignored) {}
            }
        }
    }

    public void init() {
        File defaultFile = new File(CONFIG_DIR, "default.json");

        Config defaultCfg = new Config(defaultFile, "Default");
        if (!defaultFile.exists()) {
            writeConfig(defaultCfg);
        }
        refreshConfigList();
        currentConfig = getConfigByFile(defaultFile);
        currentConfig.load();
        //        File lastDataFile = FrostCore.getClientDir().resolve("client.json").toFile();
//        boolean useDefault = false;
//        if (lastDataFile.exists()) {
//            try {
//                byte[] fileByteArray = FileUtils.readFileToByteArray(lastDataFile);
//                if (!JSON.isValid(fileByteArray)) useDefault = true;
//                else {
//                    JSONObject dataJson = JSON.parseObject(fileByteArray);
//                    String lastConfig = dataJson.getString("lastConfig");
//                    if (lastConfig == null) useDefault = true;
//                }
//            } catch (IOException e) {
//                useDefault = true;
//            }
//        } else useDefault = true;
    }

    public void saveAndWriteAllConfig() {
        for (Config config : configMap.values()) {
            config.save();
            writeConfig(config);
        }
    }
    public void writeConfig(Config config) {
        try {
            FileUtils.writeStringToFile(config.getFile(), config.getJson().toString(JSONWriter.Feature.PrettyFormat), StandardCharsets.UTF_8);
        } catch (IOException e) {}
    }
    public Config getConfigByFile(File file) {
        try {
            return configMap.get(file.toPath().toRealPath());
        } catch (IOException e) {
            return null;
        }
    }

    public boolean switchConfig(Config config) {
        if (config != null) {
            Config lastConfig = this.currentConfig;
            try {
                this.currentConfig = config;
                config.load();
                return true;
            } catch (IllegalArgumentException ex) {
                this.currentConfig = lastConfig;
                lastConfig.load();
            }
        }
        return false;
    }

    private void checkConfigPath() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
    }
}
