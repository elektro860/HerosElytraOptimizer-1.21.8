package hero.bane;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class HerosElytraOptimizerClient implements ClientModInitializer {

	private static long worldJoinTime = -1; // Time the player joined the world

	@Override
	public void onInitializeClient() {
		// Register a tick callback to track the player's world join time
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player != null && client.world != null) {
				if (worldJoinTime == -1) {
					// Set the join time if it's not already set
					worldJoinTime = System.currentTimeMillis();
				}
			} else {
				// Reset the join time when the player leaves the world
				worldJoinTime = -1;
			}
		});
	}

	/**
	 * Gets the amount of time the player has been in the world in milliseconds.
	 * @return The duration in milliseconds, or -1 if the player is not in a world.
	 */
	public static long getTimeInWorld() {
		if (worldJoinTime != -1) {
			return System.currentTimeMillis() - worldJoinTime;
		}
		return -1;
	}
}
