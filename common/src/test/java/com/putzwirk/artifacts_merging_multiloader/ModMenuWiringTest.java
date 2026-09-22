package com.putzwirk.artifacts_merging_multiloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMenuWiringTest {

    private static final String MAIN_ENTRYPOINT =
        "com.putzwirk.artifacts_merging_multiloader.ArtifactsMergingMultiloader";
    private static final String CLIENT_ENTRYPOINT =
        "com.putzwirk.artifacts_merging_multiloader.ArtifactsMergingMultiloaderClient";
    private static final String MODMENU_ENTRYPOINT =
        "com.putzwirk.artifacts_merging_multiloader.compat.ModMenuCompat";

    @Test
    void fabricModJsonDeclaresAllEntrypoints() throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(fabricModJson())).getAsJsonObject();
        JsonObject entrypoints = root.getAsJsonObject("entrypoints");
        assertNotNull(entrypoints);

        assertTrue(entrypointClasses(entrypoints, "main").contains(MAIN_ENTRYPOINT));
        assertTrue(entrypointClasses(entrypoints, "client").contains(CLIENT_ENTRYPOINT));
        assertTrue(entrypointClasses(entrypoints, "modmenu").contains(MODMENU_ENTRYPOINT));
    }

    @Test
    void modmenuCompatClassLoadsWithoutInitializing() throws Exception {
        Class<?> clazz = Class.forName(MODMENU_ENTRYPOINT, false, ModMenuWiringTest.class.getClassLoader());
        assertNotNull(clazz);
    }

    private static List<String> entrypointClasses(JsonObject entrypoints, String key) {
        JsonArray array = entrypoints.getAsJsonArray(key);
        assertNotNull(array, "missing entrypoint group " + key);
        List<String> out = new ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                out.add(element.getAsString());
            }
        }
        return out;
    }

    private static Path fabricModJson() {
        Path dir = Path.of("").toAbsolutePath();
        for (Path candidate = dir; candidate != null; candidate = candidate.getParent()) {
            Path nested = candidate.resolve("fabric/src/main/resources/fabric.mod.json");
            if (Files.isRegularFile(nested)) {
                return nested;
            }
            Path direct = candidate.resolve("src/main/resources/fabric.mod.json");
            if (Files.isRegularFile(direct)) {
                return direct;
            }
        }
        throw new IllegalStateException("fabric.mod.json not found from " + dir);
    }
}
