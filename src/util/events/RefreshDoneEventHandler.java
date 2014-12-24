package util.events;

import com.google.common.eventbus.Subscribe;

@FunctionalInterface
public interface RefreshDoneEventHandler extends EventHandler {
	@Subscribe
	public void handle(RefreshDoneEvent e);
}