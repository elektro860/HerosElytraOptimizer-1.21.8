package hero.bane;

import hero.bane.render.ElytraOptimizerHud;
import hero.bane.command.HerosElytraOptimizerCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static hero.bane.command.HerosElytraOptimizerCommand.CONFIG_PATH;

public class HerosElytraOptimizer implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("heroselytraoptimizer");

	public static MinecraftClient client;

	public static long worldJoinTime = -1;
	public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	public static boolean debugging = false;
	public static boolean instantGlide = false;
	public static float offset = 0;
	public static float pivot = 0;

	public static boolean showHud = true;
	public static int hudX = 4;
	public static int hudY = 4;
	public static ElytraOptimizerHud hud;
	public static int maxX;
	public static int maxY;
	public static int linger = 3000;

	private boolean initialized = false;

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();

		HerosElytraOptimizerCommand.loadSecondConfig();
		HerosElytraOptimizerCommand.registerCommands();
		hud = new ElytraOptimizerHud();

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.getWindow() != null && maxX == 0 && maxY == 0) {
				maxX = (int) (0.9 * client.getWindow().getScaledWidth());
				maxY = (int) (0.9 * client.getWindow().getScaledHeight());
			}

			if (client.player != null && client.world != null) {
				if (!initialized && client.getCurrentServerEntry() != null) {
					initialized = true;
					String[] vals = getCurrentServerConfig();
					offset = tryParseFloat(vals[0]);
					pivot = tryParseFloat(vals[1]);
					LOGGER.info("Loaded offset={} pivot={} for server={}", offset, pivot, client.getCurrentServerEntry().address);
				}
				if (worldJoinTime == -1) {
					worldJoinTime = System.currentTimeMillis();
					HerosElytraOptimizerCommand.reloadConfig(false);
				}
			} else {
				worldJoinTime = -1;
				initialized = false;
			}
		});
	}

	public static int getPlayerPing() {
		if (client.getNetworkHandler() != null && client.player != null) {
			PlayerListEntry playerEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
			if (playerEntry != null) {
				return playerEntry.getLatency();
			}
		}
		return 50;
	}

	public static String[] getCurrentServerConfig() {
		if (client.getCurrentServerEntry() == null) return new String[]{"0", "0"};
		String serverIP = client.getCurrentServerEntry().address;
		try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH.toFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//") || line.isBlank()) continue;
				String[] parts = line.split(" ");
				if (parts.length >= 3 && parts[0].equals(serverIP)) {
					return new String[]{parts[1], parts[2]};
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read config: {}", e.getMessage());
		}
		return new String[]{"0", "0"};
	}

	private static float tryParseFloat(String val) {
		try {
			return Float.parseFloat(val);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
