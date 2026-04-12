package pub.frost.wrappers;

@SuppressWarnings("unchecked")
public interface FakeInstanceWrapper<T> {
    default T cast(Object in) {
        return (T) in;
    }

    default <A> A cast(Object in, Class<A> clazz) {
        return clazz.cast(in);
    }
}
