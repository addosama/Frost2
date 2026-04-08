package pub.frost.client.feature.module;

import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.combat.*;
import pub.frost.client.feature.module.impl.movement.*;
import pub.frost.client.feature.module.impl.visual.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ModuleManager {
    private final Map<Class<? extends AbstractModule>, AbstractModule> moduleMap = new HashMap<>();

    public void registerModules() {
        register(
                new Sprint(),
                new HUD(),
                new ESP(),
                new JumpDelay(),
                new Velocity()
        );
        moduleMap.values().forEach(
                AbstractModule::initialize
        );
        moduleMap.values().forEach(
                module -> module.setEnabled(module.getClass().getAnnotation(Module.class).defaultState())
        );
    }

    private void register(AbstractModule... modules) {
        for (AbstractModule module : modules) {
            moduleMap.put(module.getClass(), module);
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
