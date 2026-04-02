package pub.frost.client.feature.module;

import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ModuleManager {
    private final Map<Class<? extends AbstractModule>, AbstractModule> moduleMap = new HashMap<>();

    public void registerModules() {

    }

    private void register(AbstractModule... modules) {
        for (AbstractModule module : modules) {
            moduleMap.put(module.getClass(), module);
            module.registerProperties();
        }
    }

    public <T extends AbstractModule> T getModule(Class<T> moduleClass) {
        return (T) moduleMap.get(moduleClass);
    }

    public List<AbstractModule> getRegisteredModules() {
        return getModules(m -> true);
    }
    public List<AbstractModule> getModulesByCategory(ModuleCategory category) {
        return getModules(m -> m.getCategory() == category);
    }

    public List<AbstractModule> getModules(Predicate<AbstractModule> filter) {
        return moduleMap.values().stream().filter(filter)
                .sorted(Comparator.comparing(AbstractModule::getName))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
