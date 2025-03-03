package hero.bane;

import hero.bane.command.HerosElytraOptimizerCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static hero.bane.command.HerosElytraOptimizerCommand.CONFIG_PATH;

public class HerosElytraOptimizer implements ClientModInitializer {

	public static long worldJoinTime = -1;
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
		if (client.getCurrentServerEntry() == null) {
			return new String[]{"off", "off"};
		}
		String serverIP = client.getCurrentServerEntry().address;
		try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH.toFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//") || line.isBlank()) continue; // Ignore comments and blank lines
				String[] parts = line.split(" ");
				if (parts.length >= 3 && parts[0].equals(serverIP)) {
					return new String[]{parts[1], parts[2]}; // Return glide and rocket mode
				}
			}
		} catch (IOException e) {
			System.out.println("Couldn't read it pluh");
		}
		return new String[]{"off", "off"}; // Default if no match found
	}

}
