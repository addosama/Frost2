package pub.frost.client.feature.helper.player.rotation.processors;

import java.util.function.BiConsumer;

public interface RotationProcessor {
    void process(
            float currentYaw, float currentPitch,
            float nextYaw, float nextPitch,
            BiConsumer<Float, Float> rotationAcceptor
    );
}
