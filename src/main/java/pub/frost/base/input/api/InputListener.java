package pub.frost.base.input.api;

public interface InputListener {
    default int inputPriority() {
        return 10;
    }

    default boolean onMouseButton(int button, boolean state) {
        return true;
    }
    default boolean onMouseScroll(float scrollX, float scrollY) {
        return true;
    }

    default boolean onKey(int key, boolean state) {
        return true;
    }
    default boolean onChar(char c) {
        return true;
    }
}
