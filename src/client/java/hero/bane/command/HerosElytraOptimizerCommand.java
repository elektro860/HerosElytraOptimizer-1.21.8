    package hero.bane.command;

    import com.mojang.brigadier.arguments.StringArgumentType;
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
    import java.util.List;
    import java.util.Map;

    public class HerosElytraOptimizerCommand {
        private static final MinecraftClient client = MinecraftClient.getInstance();
        private static final Logger LOGGER = LoggerFactory.getLogger(HerosElytraOptimizerCommand.class);
        public static final Path CONFIG_PATH = Paths.get(client.runDirectory.getPath(), "config", "elytraoptimizer.txt");
        private static final Path CONFIG_PATH2 = Paths.get(client.runDirectory.getPath(), "config", "elytraoptimizer2.txt");
        private static final Map<String, String[]> configMap = new HashMap<>();

        public static void registerCommands() {
            LOGGER.info("Registering Elytra Optimizer commands");
            reloadConfig(false);
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                    dispatcher.register(ClientCommandManager.literal("elytraoptimizer")
                    //Booleans
                    .then(ClientCommandManager.literal("debug")
                            .executes(ctx -> {
                                HerosElytraOptimizer.debugging = !HerosElytraOptimizer.debugging;
                                say("Debugging toggled " + (HerosElytraOptimizer.debugging ? "On" : "Off"));
                                return 0;
                            }))
                    .then(ClientCommandManager.literal("hack")
                            .executes(ctx -> {
                                HerosElytraOptimizer.shouldFlipHero = !HerosElytraOptimizer.shouldFlipHero;
                                say("Hack " + (HerosElytraOptimizer.debugging ? "Enabled" : "Disabled"));
                                return 0;
                            }))
                    .then(ClientCommandManager.literal("instaGlide")
                            .executes(ctx -> {
                                HerosElytraOptimizer.instantGlide = !HerosElytraOptimizer.instantGlide;
                                saveConfig();
                                say("Instant Glide toggled " + (HerosElytraOptimizer.instantGlide ? "On" : "Off"));
                                return 0;
                            }))
                            .then(ClientCommandManager.literal("forwardArms")
                                    .then(ClientCommandManager.argument("value", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                builder.suggest("45");
                                                builder.suggest("0");
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                String input = StringArgumentType.getString(ctx, "value").replace("°", "").trim();
                                                try {
                                                    int angle = Integer.parseInt(input);
                                                    if (angle < 0) {
                                                        angle += 360 * ((int) ((-angle) / 360f));
                                                    } else if (angle > 360) {
                                                        angle -= 360 * ((int) ((angle - 360) / 360f));
                                                    }
                                                    HerosElytraOptimizer.armAngle = angle;
                                                    HerosElytraOptimizer.superman = true;
                                                    saveConfig();
                                                    say("Forward arms set to " + angle + "°");
                                                    return 0;
                                                } catch (NumberFormatException e) {
                                                    say("Invalid input", 0xFF5555);
                                                    return 1;
                                                }
                                            }))
                                    .executes(ctx -> {
                                        HerosElytraOptimizer.superman = !HerosElytraOptimizer.superman;
                                        saveConfig();
                                        say("Superman toggled " + (HerosElytraOptimizer.superman ? "On" : "Off"));
                                        return 0;
                                    }))
                            //Hud Stuffs
                    .then(ClientCommandManager.literal("hud")
                            .executes(ctx -> {
                                HerosElytraOptimizer.showHud = !HerosElytraOptimizer.showHud;
                                saveConfig();
                                say("HUD toggled " + (HerosElytraOptimizer.showHud ? "On" : "Off"));
                                return 0;
                            })
                            .then(ClientCommandManager.literal("linger")
                                    .then(ClientCommandManager.argument("value", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                builder.suggest("reset");
                                                return builder.buildFuture();
                                            })
                                            .executes(HerosElytraOptimizerCommand::setLinger)))
                            .then(ClientCommandManager.literal("pos")
                                    .then(ClientCommandManager.argument("x", StringArgumentType.string())
                                            .suggests((context, builder) -> {
                                                builder.suggest("~");
                                                builder.suggest("reset");
                                                builder.suggest("50%");
                                                return builder.buildFuture();
                                            })
                                            .then(ClientCommandManager.argument("y", StringArgumentType.string())
                                                    .suggests((context, builder) -> {
                                                        builder.suggest("~");
                                                        builder.suggest("reset");
                                                        builder.suggest("50%");
                                                        return builder.buildFuture();
                                                    })
                                                    .executes(HerosElytraOptimizerCommand::setHudPos)))
                                    .executes(HerosElytraOptimizerCommand::setIndentMode)))
                    //Floats
                    .then(ClientCommandManager.literal("offset")
                            .then(ClientCommandManager.argument("value", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        builder.suggest("reset");
                                        return builder.buildFuture();
                                    })
                                    .executes(HerosElytraOptimizerCommand::setOffset)))
                    .then(ClientCommandManager.literal("pivot")
                            .then(ClientCommandManager.argument("value", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        builder.suggest("reset");
                                        return builder.buildFuture();
                                    })
                                    .executes(HerosElytraOptimizerCommand::setPivot)))
                    //Server Specific
                    .then(ClientCommandManager.literal("glide")
                            .then(ClientCommandManager.literal("off").executes(ctx -> updateConfig(ctx, "glide", "off")))
                            .then(ClientCommandManager.literal("delayed").executes(ctx -> updateConfig(ctx, "glide", "delayed")))
                            .then(ClientCommandManager.literal("on").executes(ctx -> updateConfig(ctx, "glide", "on"))))
                    .then(ClientCommandManager.literal("rocket")
                            .then(ClientCommandManager.literal("off").executes(ctx -> updateConfig(ctx, "rocket", "off")))
                            .then(ClientCommandManager.literal("delayed").executes(ctx -> updateConfig(ctx, "rocket", "delayed")))
                            .then(ClientCommandManager.literal("on").executes(ctx -> updateConfig(ctx, "rocket", "on"))))
                    //Config Crap
                    .then(ClientCommandManager.literal("save")
                            .executes(ctx -> reloadConfig(true)))
                    .then(ClientCommandManager.literal("open")
                            .executes(HerosElytraOptimizerCommand::openConfig))
            ));
        }

        private static int setIndentMode(CommandContext<FabricClientCommandSource> ctx) {
            String val = StringArgumentType.getString(ctx, "value").toLowerCase();
            HerosElytraOptimizer.HudIndent indent = HerosElytraOptimizer.HudIndent.fromString(val);
            if (indent == null) {
                say("Invalid indent. Use left, centered, or right.", 0xFF5555);
                return 1;
            }
            HerosElytraOptimizer.indentMode = indent;
            saveConfig();
            say("HUD indent set to " + val);
            return 0;
        }

        private static int setLinger(CommandContext<FabricClientCommandSource> ctx) {
            String input = StringArgumentType.getString(ctx, "value");

            if (input.equalsIgnoreCase("reset")) {
                HerosElytraOptimizer.linger = 3000;
                saveConfig();
                say("Linger reset to 1000ms");
                return 0;
            }

            try {
                int value = Integer.parseInt(input);
                value = Math.max(250, Math.min(value, 10000));
                HerosElytraOptimizer.linger = value;
                saveConfig();
                say("Linger set to " + value + "ms");
                return 0;
            } catch (NumberFormatException e) {
                say("Invalid Input for linger", 0xFF5555);
                return 1;
            }
        }

        private static int setHudPos(CommandContext<FabricClientCommandSource> ctx) {
            String inputX = StringArgumentType.getString(ctx, "x");
            String inputY = StringArgumentType.getString(ctx, "y");

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            HerosElytraOptimizer.maxX = (int) (0.9 * screenWidth);
            HerosElytraOptimizer.maxY = (int) (0.9 * screenHeight);

            try {
                if (inputX.equalsIgnoreCase("reset")) {
                    HerosElytraOptimizer.hudX = 4;
                } else if (!inputX.equals("~")) {
                    HerosElytraOptimizer.hudX = parseDimension(inputX, HerosElytraOptimizer.maxX);
                }

                if (inputY.equalsIgnoreCase("reset")) {
                    HerosElytraOptimizer.hudY = 4;
                } else if (!inputY.equals("~")) {
                    HerosElytraOptimizer.hudY = parseDimension(inputY, HerosElytraOptimizer.maxY);
                }

                saveConfig();
                say("HUD position set to (%d, %d)".formatted(HerosElytraOptimizer.hudX, HerosElytraOptimizer.hudY));
                return 0;
            } catch (IllegalArgumentException e) {
                say("Invalid HUD position: " + e.getMessage(), 0xFF5555);
                return 1;
            }
        }

        private static int parseDimension(String input, int max) {
            input = input.trim();
            if (input.endsWith("%")) {
                float percent = Float.parseFloat(input.substring(0, input.length() - 1));
                return Math.round(percent / 100f * max);
            }
            return Integer.parseInt(input);
        }

        private static int setOffset(CommandContext<FabricClientCommandSource> ctx) {
            String input = StringArgumentType.getString(ctx, "value");

            if (input.equalsIgnoreCase("reset")) {
                HerosElytraOptimizer.offset = 0.0f;
                saveConfig();
                say("Offset reset to 0");
                return 0;
            }

            try {
                HerosElytraOptimizer.offset = Math.clamp(Float.parseFloat(input), 0.0f, 0.59f);
                saveConfig();
                say("Offset set to " + HerosElytraOptimizer.offset);
                return 0;
            } catch (NumberFormatException e) {
                say("Invalid Input", 0xFF5555);
                return 1;
            }
        }

        private static int setPivot(CommandContext<FabricClientCommandSource> ctx) {
            String input = StringArgumentType.getString(ctx, "value");

            if (input.equalsIgnoreCase("reset")) {
                HerosElytraOptimizer.pivot = 0.0f;
                saveConfig();
                say("Pivot reset to 0");
                return 0;
            }

            try {
                HerosElytraOptimizer.pivot = Math.clamp(Float.parseFloat(input), 0.0f, 1.79f);
                saveConfig();
                say("Pivot set to " + HerosElytraOptimizer.pivot);
                return 0;
            } catch (NumberFormatException e) {
                say("Invalid Input", 0xFF5555);
                return 1;
            }
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

        @SuppressWarnings("DuplicateExpressions")
        public static void loadSecondConfig() {
            try {
                boolean created = false;

                if (!Files.exists(CONFIG_PATH2)) {
                    Files.createDirectories(CONFIG_PATH2.getParent());
                    Files.writeString(CONFIG_PATH2, """
                // Don't touch this - use in game commands
                offset: 0
                pivot: 0
                instantGlide: false
                """);
                    created = true;
                }

                List<String> lines = Files.readAllLines(CONFIG_PATH2).stream()
                        .map(String::trim)
                        .filter(line -> !line.startsWith("//") && !line.isEmpty())
                        .toList();

                float offset = 0f;
                float pivot = 0f;
                boolean instantGlide = false;
                int hudX = 4;
                int hudY = 4;
                boolean showHud = true;
                int linger = 3000;
                HerosElytraOptimizer.HudIndent indent = HerosElytraOptimizer.HudIndent.LEFT;
                boolean superman = false;

                for (String line : lines) {
                    if (line.startsWith("offset:")) {
                        offset = parseOrZero(line.substring(7), 0.6F);
                    } else if (line.startsWith("pivot:")) {
                        pivot = parseOrZero(line.substring(6), 1.8F);
                    } else if (line.startsWith("instantGlide:")) {
                        instantGlide = Boolean.parseBoolean(line.substring(13).trim());
                    } else if (line.startsWith("hudX:")) {
                        hudX = Integer.parseInt(line.substring(5).trim());
                    } else if (line.startsWith("hudY:")) {
                        hudY = Integer.parseInt(line.substring(5).trim());
                    } else if (line.startsWith("showHud:")) {
                        showHud = Boolean.parseBoolean(line.substring(8).trim());
                    } else if (line.startsWith("linger:")) {
                        try {
                            int val = Integer.parseInt(line.substring(7).trim());
                            linger = Math.max(200, Math.min(val, 5000));
                        } catch (NumberFormatException ignored) {}
                    } else if (line.startsWith("indent:")) {
                        HerosElytraOptimizer.HudIndent parsed = HerosElytraOptimizer.HudIndent.fromString(line.substring(7).trim());
                        if (parsed != null) indent = parsed;
                    } else if (line.startsWith("superman:")) {
                        superman = Boolean.parseBoolean(line.substring(9).trim());
                    } else if (line.startsWith("supermanAngle:")) {
                        try {
                            int angle = Integer.parseInt(line.substring(14).trim());
                            if (angle < 0) {
                                angle += 360 * ((int) ((-angle) / 360f));
                            } else if (angle > 360) {
                                angle -= 360 * ((int) ((angle - 360) / 360f));
                            }
                            HerosElytraOptimizer.armAngle = angle;
                        } catch (NumberFormatException ignored) {}
                    }
                }

                HerosElytraOptimizer.offset = offset;
                HerosElytraOptimizer.pivot = pivot;
                HerosElytraOptimizer.instantGlide = instantGlide;
                HerosElytraOptimizer.hudX = hudX;
                HerosElytraOptimizer.hudY = hudY;
                HerosElytraOptimizer.showHud = showHud;
                HerosElytraOptimizer.linger = linger;
                HerosElytraOptimizer.indentMode = indent;
                HerosElytraOptimizer.superman = superman;

                if (created) saveConfig();
            } catch (IOException e) {
                LOGGER.error("Failed to load elytraoptimizer2.txt", e);
                HerosElytraOptimizer.offset = 0f;
                HerosElytraOptimizer.pivot = 0f;
                HerosElytraOptimizer.instantGlide = false;
            }
        }

        private static void saveConfig() {
            try {
                String configText = """
            // Don't touch this - use in game commands
            offset: %s
            pivot: %s
            instantGlide: %s
            hudX: %s
            hudY: %s
            showHud: %s
            linger: %s
            indent: %s
            superman: %s
            supermanAngle: %s
            """.formatted(
                        HerosElytraOptimizer.offset,
                        HerosElytraOptimizer.pivot,
                        HerosElytraOptimizer.instantGlide,
                        HerosElytraOptimizer.hudX,
                        HerosElytraOptimizer.hudY,
                        HerosElytraOptimizer.showHud,
                        HerosElytraOptimizer.linger,
                        HerosElytraOptimizer.indentMode.toString(),
                        HerosElytraOptimizer.superman,
                        HerosElytraOptimizer.armAngle
                );

                Files.writeString(CONFIG_PATH2, configText.stripTrailing());
            } catch (IOException e) {
                LOGGER.error("Failed to save elytraoptimizer2.txt", e);
            }
        }

        private static float parseOrZero(String val, float max) {
            try {
                float v = Float.parseFloat(val.trim());
                return (v >= 0 && v < max) ? v : 0;
            } catch (NumberFormatException e) {
                return 0;
            }
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
