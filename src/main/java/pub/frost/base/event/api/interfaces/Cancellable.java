package pub.frost.base.event.api.interfaces;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
    default void cancel() {
        setCancelled(true);
    }
}
