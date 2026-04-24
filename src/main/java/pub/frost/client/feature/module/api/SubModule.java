package pub.frost.client.feature.module.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubModule<PARENT extends AbstractModule> {
    protected final PARENT parent;
}
