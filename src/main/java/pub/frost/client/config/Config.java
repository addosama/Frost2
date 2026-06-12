package pub.frost.client.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.suppliers.OverrideOnKey;

import java.io.File;
import java.util.function.Function;

@Getter
public class Config {
    private final File file;
    private final String name;
    private JSONObject json;

    public Config(File file, JSONObject json) {
        String name = json.getString("name");
        if (name == null) name = file.getName();

        this.file = file;
        this.name = name;
        this.json = json;
    }

    public Config(File file, String name) {
        this.file = file;
        this.name = name;
        save();
    }

    public void save() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        // modules
        {
            JSONObject modules = new JSONObject();
            for (AbstractModule m : FrostCore.getInstance().getModuleManager().getRegisteredModules()) {
                modules.put(m.getKey(), m.getPropertyList());
            }
            jsonObject.put("modules", modules);
        }

        // client
        {
            JSONObject client = new JSONObject();
            for (PropertyDescriptor descriptor : FrostCore.getInstance().getClientSettings().getDescMap().values()) {
                client.put(descriptor.getKey(), descriptor.getChildProperties());
            }
            jsonObject.put("client", client);
        }

        this.json = JSON.parseObject(jsonObject.toJSONString(JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.WriteEnumUsingToString, JSONWriter.Feature.FieldBased));
    }

    public void load() {
        // modules
        {
            JSONObject moduleData = json.getJSONObject("modules");
            if (moduleData == null) invalidConfig();

            for (AbstractModule m : FrostCore.getInstance().getModuleManager().getRegisteredModules()) {
                JSONArray propArray = moduleData.getJSONArray(m.getKey());
                if (propArray == null) continue;
                for (JSONObject propJson : propArray.toJavaList(JSONObject.class)) {
                    analyzeDescriptorData(propJson, m::getDescriptor);
                }
            }
        }

        // client
        {
            JSONObject clientData = json.getJSONObject("client");
            if (clientData == null) invalidConfig();

            for (PropertyDescriptor descriptor : FrostCore.getInstance().getClientSettings().getDescMap().values()) {
                JSONArray propArray = clientData.getJSONArray(descriptor.getKey());
                if (propArray == null) continue;
                for (JSONObject propJson : propArray.toJavaList(JSONObject.class)) {
                    analyzeDescriptorData(propJson, FrostCore.getInstance().getClientSettings()::getDescriptor);
                }
            }
        }
    }

    private boolean analyzeDescriptorData(JSONObject propJson, Function<String, PropertyDescriptor> descriptorGetter) {
        String propKey = propJson.getString("key");
        if (propKey == null) return false;
        PropertyDescriptor descriptor = descriptorGetter.apply(propKey);
        if (descriptor == null) return false;
        if (descriptor.isGroup()) {
            JSONArray childListJson = propJson.getJSONArray("childList");
            if (childListJson == null) return false;
            boolean result = true;
            for (JSONObject childData : childListJson.toJavaList(JSONObject.class)) {
                result &= analyzeDescriptorData(childData, descriptorGetter);
            }
            return result;
        } else {
            JSONObject dataJson = propJson.getJSONObject("data");
            if (dataJson == null) return false;
            AbstractProperty prop = descriptor.getProperty();
            // read value
            {
                Object value = dataJson.get("value");
                if (value == null) return false;
                prop.set(prop.deserializeValue(value));
            }
            // read override
            {
                prop.clearOverridingData();
                JSONObject overridingJson = dataJson.getJSONObject("overriding");
                if (overridingJson == null) return false;
                JSONArray overridingDataArray = overridingJson.getJSONArray("data");
                if (overridingDataArray != null) {
                    for (JSONObject data : overridingDataArray.toJavaList(JSONObject.class)) {
                        OverrideOnKey supplier; {
                            JSONObject supplierJson = data.getJSONObject("supplier");
                            Integer keyCode = supplierJson.getInteger("keycode");
                            Boolean requireHold = supplierJson.getBoolean("requireHold");
                            if (keyCode == null || requireHold == null) continue;
                            supplier = new OverrideOnKey(keyCode, requireHold);
                        }
                        Object value; {
                            value = data.get("value");
                            if (value == null) continue;
                            value = prop.deserializeValue(value);
                        }
                        prop.addOverrideData(new OverrideData(supplier, value));
                    }
                }
            }
            return true;
        }
    }

    private void invalidConfig() {
        throw new IllegalArgumentException("not a valid config json");
    }
}
