package com.putzwirk.artifacts_merging_multiloader.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MergeConfigPacketTest {

    @TempDir
    Path tempDir;

    private record Snapshot(String id, int count, List<String> items, Map<String, String> names) {
    }

    private static List<Snapshot> snapshot() {
        List<Snapshot> out = new ArrayList<>();
        for (MergeEntry entry : MergeConfigManager.groups()) {
            out.add(new Snapshot(entry.id, entry.count, List.copyOf(entry.items), new LinkedHashMap<>(entry.names)));
        }
        return out;
    }

    @Test
    void exportedConfigRoundTripsThroughApplyRemote() throws Exception {
        MergeConfigManager.load(tempDir);
        List<Snapshot> before = snapshot();
        byte[] iconBytes = Files.readAllBytes(MergeConfigManager.root().resolve("artifacts/random_artifact.png"));

        String json = MergeConfigManager.exportJson();
        MergeConfigManager.applyRemote(json);

        assertEquals(before, snapshot());

        MergeEntry artifacts = MergeConfigManager.byId("artifacts/artifacts");
        assertNotNull(artifacts);
        assertNotNull(artifacts.iconData);
        assertArrayEquals(iconBytes, artifacts.iconData);
    }

    @Test
    void applyRemoteIgnoresGroupsWithoutItems() {
        MergeConfigManager.load(tempDir);
        List<Snapshot> before = snapshot();

        MergeConfigManager.applyRemote("[{\"id\":\"x\"}]");

        assertEquals(before, snapshot());
    }

    @Test
    void applyRemoteIgnoresInvalidJson() {
        MergeConfigManager.load(tempDir);
        List<Snapshot> before = snapshot();

        MergeConfigManager.applyRemote("not json");

        assertEquals(before, snapshot());
    }
}
