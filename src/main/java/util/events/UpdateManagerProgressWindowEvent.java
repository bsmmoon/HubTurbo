package util.events;

public class UpdateManagerProgressWindowEvent extends Event {
    public final boolean isProgressWindowEnabled;

    public UpdateManagerProgressWindowEvent(boolean isProgressWindowEnabled) {
        this.isProgressWindowEnabled = isProgressWindowEnabled;
    }
}
