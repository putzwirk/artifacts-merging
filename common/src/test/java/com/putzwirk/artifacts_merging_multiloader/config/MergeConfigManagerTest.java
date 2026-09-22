package com.putzwirk.artifacts_merging_multiloader.config;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MergeConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void copiesBundledDefaultsIntoEmptyConfigDir() {
        MergeConfigManager.load(tempDir);

        List<String> ids = MergeConfigManager.groups().stream().map(entry -> entry.id).toList();
        assertEquals(List.of(
            "artifacts/artifacts",
            "losttrinkets/losttrinkets",
            "nameless_trinkets/nameless_trinkets",
            "relics/relics",
            "relics_artifacts/relics_artifacts"), ids);

        MergeEntry artifacts = MergeConfigManager.byId("artifacts/artifacts");
        assertNotNull(artifacts);
        assertEquals(48, artifacts.items.size());
        assertEquals(2, artifacts.count);

        Path icon = artifacts.resolveIcon(MergeConfigManager.root());
        assertNotNull(icon);
        assertEquals("random_artifact.png", icon.getFileName().toString());
    }

    @Test
    void stripsCommentsDeduplicatesItemsAndClampsCounts() throws Exception {
        Path root = tempDir.resolve(Constants.CONFIG_DIR_NAME);
        Files.createDirectories(root.resolve("x"));
        Files.writeString(root.resolve("x/one.json"), """
            {
              // line comment must be removed
              "count": 99,
              /* block comment must be removed too */
              "items": ["minecraft:diamond", "minecraft:diamond", "minecraft:emerald"],
              "translations": {"en_us": "One", "de_de": "Eins"}
            }
            """);
        Files.writeString(root.resolve("x/two.json"), """
            {
              "count": 0,
              "items": ["minecraft:gold_ingot"],
              "translations": [
                {"en_us": "Two"},
                {"de_de": "Zwei"}
              ]
            }
            """);

        MergeConfigManager.load(tempDir);

        MergeEntry one = MergeConfigManager.byId("x/one");
        assertNotNull(one);
        assertEquals(9, one.count);
        assertEquals(List.of("minecraft:diamond", "minecraft:emerald"), one.items);
        assertEquals("One", one.displayName("en_us"));
        assertEquals("Eins", one.displayName("de_de"));

        MergeEntry two = MergeConfigManager.byId("x/two");
        assertNotNull(two);
        assertEquals(1, two.count);
        assertEquals(List.of("minecraft:gold_ingot"), two.items);
        assertEquals("Two", two.displayName("en_us"));
        assertEquals("Zwei", two.displayName("de_de"));
    }
}
