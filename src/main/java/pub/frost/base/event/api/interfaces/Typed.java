package pub.frost.base.event.api.interfaces;

public interface Typed<T extends Enum<?>> {
    T getType();
}