package pub.frost.utils.data;

import lombok.Getter;
import pub.frost.utils.MathUtils;

@Getter
public class Rotation {
    private final float yaw, pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = MathUtils.clamp(pitch, -90f, 90f);
    }
}
