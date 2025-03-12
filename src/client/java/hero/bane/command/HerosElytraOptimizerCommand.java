package hero.bane.command;

import com.mojang.brigadier.context.CommandContext;
import hero.bane.HerosElytraOptimizer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HerosElytraOptimizerCommand {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(HerosElytraOptimizerCommand.class);
    public static final Path CONFIG_PATH = Paths.get(client.runDirectory.getPath(), "config", "elytraoptimizer.txt");
    private static final Map<String, String[]> configMap = new HashMap<>();

    public static void registerCommands() {
        LOGGER.info("Registering Elytra Optimizer commands");
        reloadConfig(false); //just so it reloads config when initialized
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("elytraoptimizer")
                    .then(ClientCommandManager.literal("debug")
                            .executes(ctx -> {
                                HerosElytraOptimizer.debugging = !HerosElytraOptimizer.debugging;
                                say("Debugging toggled "+(HerosElytraOptimizer.debugging ? "On" : "Off"));
                                return 0;
                            }))
                    .then(ClientCommandManager.literal("save")
                            .executes(ctx -> reloadConfig(true)))
                    .then(ClientCommandManager.literal("open")
                            .executes(HerosElytraOptimizerCommand::openConfig))
                    .then(ClientCommandManager.literal("glide")
                            .then(ClientCommandManager.literal("off").executes(ctx -> updateConfig(ctx, "glide", "off")))
                            .then(ClientCommandManager.literal("delayed").executes(ctx -> updateConfig(ctx, "glide", "delayed")))
                            .then(ClientCommandManager.literal("on").executes(ctx -> updateConfig(ctx, "glide", "on"))))
                    .then(ClientCommandManager.literal("rocket")
                            .then(ClientCommandManager.literal("off").executes(ctx -> updateConfig(ctx, "rocket", "off")))
                            .then(ClientCommandManager.literal("delayed").executes(ctx -> updateConfig(ctx, "rocket", "delayed")))
                            .then(ClientCommandManager.literal("on").executes(ctx -> updateConfig(ctx, "rocket", "on")))));
        });
    }

    private static int updateConfig(CommandContext<FabricClientCommandSource> ctx, String type, String value) {
        FabricClientCommandSource source = ctx.getSource();
        String serverIP = source.getClient().getCurrentServerEntry() != null ? source.getClient().getCurrentServerEntry().address : null;

        if (serverIP == null) {
            source.sendFeedback(net.minecraft.text.Text.of("[ElytraOptimizer] You must be on a server to use this command."));
            return 1;
        }

        configMap.putIfAbsent(serverIP, new String[]{"off", "off"});

        switch (type) {
            case "glide" -> configMap.get(serverIP)[0] = value;
            case "rocket" -> configMap.get(serverIP)[1] = value;
        }

        if ("off".equals(configMap.get(serverIP)[0]) && "off".equals(configMap.get(serverIP)[1])) {
            configMap.remove(serverIP);
        }

        int result = reloadConfig(true);

        say("Updated for " + serverIP +
                ":\n Gliding Optimization=" + configMap.getOrDefault(serverIP, new String[]{"off", "off"})[0] +
                ",\n Rocket Optimization=" + configMap.getOrDefault(serverIP, new String[]{"off", "off"})[1]);

        return result;
    }


    public static int reloadConfig(boolean save) {
        try {
            if (save) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_PATH.toFile()))) {
                    for (Map.Entry<String, String[]> entry : configMap.entrySet()) {
                        writer.write(entry.getKey() + " " + entry.getValue()[0] + " " + entry.getValue()[1]);
                        writer.newLine();
                    }
                    say("Elytra Optimizer config saved.");
                    return 0;
                }
            }

            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.writeString(CONFIG_PATH, """
                    // Lines starting with // are ignored
                    // Format: <server> <glide mode> <rocket mode>
                    // Available modes: off, delayed, on
                    // Please confirm with server staff if allowed before enabling
                    // Still working on server side opt-outs
                    vanilla.mctiers.com on delayed
                    eu.vanilla.mctiers.com on delayed
                    """);
            }

            configMap.clear();

            try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("//") || line.isBlank()) continue;
                    String[] parts = line.split(" ");
                    if (parts.length >= 3) {
                        configMap.put(parts[0], new String[]{parts[1], parts[2]});
                    }
                }
            }
            return 0;
        } catch (IOException e) {
            LOGGER.error("Failed to load or save Elytra Optimizer config", e);
            return 1;
        }
    }


    private static int openConfig(CommandContext<FabricClientCommandSource> context) {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            say("Config file not found.");
            return 1;
        }

        new Thread(() -> {
            try {
                Util.getOperatingSystem().open(file);
            } catch (Exception e) {
                LOGGER.error("Error opening Elytra Optimizer config file", e);
            }
        }).start();

        return 0;
    }

    public static void say(String message) {
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.of("[ElytraOptimizer] " + message), false);
        }
    }

    public static void say(String message, int color) {
        if (client.player != null) {
            client.player.sendMessage(net.minecraft.text.Text.literal("[ElytraOptimizer] " + message)
                    .styled(style -> style.withColor(color)), false);
        }
    }
}
