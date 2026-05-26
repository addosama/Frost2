package pub.frost.client.feature.module.api;

import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.SubModule;

public class AbstractSubModule<PARENT extends AbstractModule> implements Wrappers {
    /**
     * @deprecated
     * consider using <code>getParent()</code> with no args constructor
     */
    @Deprecated
    protected final PARENT parent;

    private PARENT p;
    protected final Object mc = Minecraft.getInstance();


    /**
     * @deprecated
     * consider using <code>@SubModule</code> annotation with no args constructor
     */
    @Deprecated
    public AbstractSubModule(PARENT parent) {
        this.parent = parent;
    }

    public AbstractSubModule() {
        SubModule annotation = this.getClass().getAnnotation(SubModule.class);
        FrostCore.debugAssert(annotation != null, "Missing @SubModule annotation");
        this.parent = null;
    }

    @SuppressWarnings("unchecked")
    public PARENT getParent() {
        if (p == null) {
            p = FrostCore.getInstance().getModuleManager().getModule(
                    (Class<PARENT>) this.getClass().getAnnotation(SubModule.class).value()
            );
        }
        return p;
    }
}
