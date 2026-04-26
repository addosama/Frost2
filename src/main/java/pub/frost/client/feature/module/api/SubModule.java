package pub.frost.client.feature.module.api;

import lombok.RequiredArgsConstructor;
import pub.frost.base.wrapping.Wrappers;

@RequiredArgsConstructor
public class SubModule<PARENT extends AbstractModule> implements Wrappers {
    protected final PARENT parent;
    protected final Object mc = Minecraft.getInstance();
}
