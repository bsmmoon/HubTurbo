package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface UpdateManagerProgressWindowEventHandler extends EventHandler {
    @Subscribe
    void handle(UpdateManagerProgressWindowEvent e);
}
