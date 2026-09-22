package com.putzwirk.artifacts_merging_multiloader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.putzwirk.artifacts_merging_multiloader.Constants;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MergeConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<MergeEntry> GROUPS = new ArrayList<>();
    private static final int MAX_ICON_BYTES = 64 * 1024;
    private static final int MAX_TOTAL_ICON_BYTES = 256 * 1024;
    private static final String DEFAULT_RESOURCE_ROOT = "/assets/artifactsmerging/defaults/";
    private static final List<String> DEFAULT_RESOURCES = List.of(
        "artifacts/artifacts.json",
        "artifacts/random_artifact.png",
        "losttrinkets/losttrinkets.json",
        "nameless_trinkets/nameless_trinkets.json",
        "relics/relics.json",
        "relics_artifacts/relics_artifacts.json"
    );
    private static Path root = null;
    private static Path localConfigDir = null;

    public static List<MergeEntry> groups() {
        return Collections.unmodifiableList(GROUPS);
    }

    public static Path root() {
        return root;
    }

    public static void load(Path gameConfigDir) {
        localConfigDir = gameConfigDir;
        root = gameConfigDir.resolve(Constants.CONFIG_DIR_NAME);
        GROUPS.clear();
        try {
            Files.createDirectories(root);
            if (!hasMergeFiles(root)) {
                copyDefaults(root);
            }
            try (Stream<Path> walk = Files.walk(root, 4)) {
                walk.filter(Files::isRegularFile)
                    .filter(MergeConfigManager::isMergeFile)
                    .sorted()
                    .forEach(MergeConfigManager::loadFile);
            }
        } catch (Exception e) {
            Constants.LOG.warn("Failed to load merge configs from {}: {}", root, e.toString());
        }
        Constants.LOG.info("Loaded {} merge group(s) from {}", GROUPS.size(), root);
    }

    public static void reloadLocal() {
        if (localConfigDir != null) {
            load(localConfigDir);
        }
    }

    @Nullable
    public static MergeEntry byId(String id) {
        for (MergeEntry entry : GROUPS) {
            if (entry.id.equals(id)) {
                return entry;
            }
        }
        return null;
    }

    public static String exportJson() {
        JsonArray out = new JsonArray();
        int iconBudget = MAX_TOTAL_ICON_BYTES;
        for (MergeEntry entry : GROUPS) {
            JsonObject json = new JsonObject();
            json.addProperty("id", entry.id);
            if (entry.hasIconReference()) {
                json.addProperty("icon", entry.icon);
            }
            json.addProperty("count", entry.count);
            JsonArray items = new JsonArray();
            for (String id : entry.items) {
                items.add(id);
            }
            json.add("items", items);
            if (!entry.names.isEmpty()) {
                JsonObject translations = new JsonObject();
                for (Map.Entry<String, String> name : entry.names.entrySet()) {
                    translations.addProperty(name.getKey(), name.getValue());
                }
                json.add("translations", translations);
            }
            byte[] icon = iconBytes(entry);
            if (icon != null && icon.length <= MAX_ICON_BYTES && icon.length <= iconBudget) {
                json.addProperty("iconData", Base64.getEncoder().encodeToString(icon));
                iconBudget -= icon.length;
            }
            out.add(json);
        }
        return GSON.toJson(out);
    }

    public static void applyRemote(String json) {
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonArray()) {
                return;
            }
            List<MergeEntry> remote = new ArrayList<>();
            for (JsonElement element : parsed.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                if (!object.has("id") || !object.get("id").isJsonPrimitive()) {
                    continue;
                }
                MergeEntry entry = parseEntry(object.get("id").getAsString(), null, object);
                if (object.has("iconData") && object.get("iconData").isJsonPrimitive()) {
                    try {
                        entry.iconData = Base64.getDecoder().decode(object.get("iconData").getAsString());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (!entry.items.isEmpty()) {
                    remote.add(entry);
                }
            }
            if (remote.isEmpty()) {
                Constants.LOG.warn("Ignoring empty merge config received from server");
                return;
            }
            GROUPS.clear();
            GROUPS.addAll(remote);
            Constants.LOG.info("Applied {} merge group(s) received from server", remote.size());
        } catch (Exception e) {
            Constants.LOG.warn("Failed to apply merge config received from server: {}", e.toString());
        }
    }

    @Nullable
    private static byte[] iconBytes(MergeEntry entry) {
        if (entry.iconData != null) {
            return entry.iconData;
        }
        if (root == null) {
            return null;
        }
        Path file = entry.resolveIcon(root);
        if (file == null) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (Exception e) {
            return null;
        }
    }

    private static void loadFile(Path file) {
        try {
            String raw = Files.readString(file);
            JsonObject json = GSON.fromJson(stripComments(raw), JsonObject.class);
            if (json == null) {
                return;
            }
            Path groupDir = file.getParent();
            String relative = root.relativize(file).toString().replace('\\', '/');
            String id = relative.substring(0, relative.lastIndexOf('.'));
            MergeEntry entry = parseEntry(id, groupDir, json);
            if (entry.items.isEmpty()) {
                Constants.LOG.warn("Skipping merge config {}: no items", file);
                return;
            }
            GROUPS.add(entry);
        } catch (Exception e) {
            Constants.LOG.warn("Skipping unreadable merge config {}: {}", file, e.toString());
        }
    }

    private static MergeEntry parseEntry(String id, @Nullable Path groupDir, JsonObject json) {
        String icon = json.has("icon") && json.get("icon").isJsonPrimitive()
            ? json.get("icon").getAsString()
            : "";
        int count = json.has("count") ? Math.max(1, Math.min(9, json.get("count").getAsInt())) : 2;
        List<String> items = new ArrayList<>();
        if (json.has("items") && json.get("items").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("items")) {
                if (element.isJsonPrimitive()) {
                    String value = element.getAsString().trim();
                    if (!value.isEmpty() && !items.contains(value)) {
                        items.add(value);
                    }
                }
            }
        }
        Map<String, String> names = new LinkedHashMap<>();
        if (json.has("translations")) {
            JsonElement translations = json.get("translations");
            if (translations.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : translations.getAsJsonObject().entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        names.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            } else if (translations.isJsonArray()) {
                for (JsonElement element : translations.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                names.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                    }
                }
            }
        }
        return new MergeEntry(id, icon, count, items, names, groupDir);
    }

    private static boolean isMergeFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".json") || name.endsWith(".jsonc") || name.endsWith(".json5");
    }

    private static boolean hasMergeFiles(Path dir) throws Exception {
        try (Stream<Path> walk = Files.walk(dir, 4)) {
            return walk.filter(Files::isRegularFile).anyMatch(MergeConfigManager::isMergeFile);
        }
    }

    private static void copyDefaults(Path dir) {
        for (String relative : DEFAULT_RESOURCES) {
            try (InputStream in = MergeConfigManager.class.getResourceAsStream(DEFAULT_RESOURCE_ROOT + relative)) {
                if (in == null) {
                    Constants.LOG.warn("Missing bundled default merge config: {}", relative);
                    continue;
                }
                Path target = dir.resolve(relative);
                Files.createDirectories(target.getParent());
                if (Files.notExists(target)) {
                    Files.copy(in, target);
                }
            } catch (Exception e) {
                Constants.LOG.warn("Failed to write default merge config {}: {}", relative, e.toString());
            }
        }
    }

    static String stripComments(String raw) {
        StringBuilder out = new StringBuilder(raw.length());
        boolean inString = false;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            char next = i + 1 < raw.length() ? raw.charAt(i + 1) : 0;
            if (lineComment) {
                if (c == '\n') {
                    lineComment = false;
                    out.append(c);
                }
                continue;
            }
            if (blockComment) {
                if (c == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                out.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }
            if (c == '/' && next == '/') {
                lineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                blockComment = true;
                i++;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
