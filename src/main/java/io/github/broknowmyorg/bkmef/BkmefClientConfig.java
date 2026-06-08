package io.github.broknowmyorg.bkmef;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BkmefClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "broknowmyemifolder-client.json";

    private static boolean foldingEnabled = true;
    private static boolean reloadMessagesEnabled = true;

    private BkmefClientConfig() {
    }

    public static void load() {
        Path configPath = configPath();
        if (!Files.isRegularFile(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            foldingEnabled = getBoolean(json, "foldingEnabled", foldingEnabled);
            reloadMessagesEnabled = getBoolean(json, "reloadMessagesEnabled", reloadMessagesEnabled);
        } catch (Exception exception) {
            Broknowmyemifolder.LOGGER.warn("Failed to load BKMEF client config, using defaults", exception);
        }
    }

    public static void save() {
        try {
            Path configPath = configPath();
            Files.createDirectories(configPath.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("foldingEnabled", foldingEnabled);
            json.addProperty("reloadMessagesEnabled", reloadMessagesEnabled);
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception exception) {
            Broknowmyemifolder.LOGGER.warn("Failed to save BKMEF client config", exception);
        }
    }

    public static boolean isFoldingEnabled() {
        return foldingEnabled;
    }

    public static void setFoldingEnabled(boolean value) {
        foldingEnabled = value;
        save();
    }

    public static boolean isReloadMessagesEnabled() {
        return reloadMessagesEnabled;
    }

    public static void setReloadMessagesEnabled(boolean value) {
        reloadMessagesEnabled = value;
        save();
    }

    private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsBoolean() : fallback;
    }

    private static Path configPath() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        return (configDir == null ? Path.of("config") : configDir).resolve(CONFIG_FILE);
    }
}
