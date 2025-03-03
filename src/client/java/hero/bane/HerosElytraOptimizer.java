package hero.bane;

import hero.bane.command.HerosElytraOptimizerCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class HerosElytraOptimizer implements ClientModInitializer {

	public static long worldJoinTime = -1;
	public static Map<String, String[]> configMap = new HashMap<>();
	public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	public static boolean debugging = false;

	@Override
	public void onInitializeClient() {
		HerosElytraOptimizerCommand.registerCommands();
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player != null && client.world != null) {
				if (worldJoinTime == -1) {
					worldJoinTime = System.currentTimeMillis();
				}
			} else {
				worldJoinTime = -1;
			}
		});

		HerosElytraOptimizerCommand.reloadConfig(false);
	}

	public static int getPlayerPing() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() != null && client.player != null) {
			PlayerListEntry playerEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
			if (playerEntry != null) {
				return playerEntry.getLatency();
			}
		}
		return 50;
	}

	public static String[] getCurrentServerConfig() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getCurrentServerEntry() != null) {
			return configMap.getOrDefault(client.getCurrentServerEntry().address, new String[]{"off", "off"});
		}
		return new String[]{"off", "off"};
	}
}
