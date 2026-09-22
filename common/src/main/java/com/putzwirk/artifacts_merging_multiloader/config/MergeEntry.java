package com.putzwirk.artifacts_merging_multiloader.config;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MergeEntry {
    public String id = "";
    public String icon = "";
    public int count = 2;
    public List<String> items = new ArrayList<>();
    public Map<String, String> names = new LinkedHashMap<>();
    @Nullable
    public Path sourceDir = null;
    @Nullable
    public byte[] iconData = null;

    public MergeEntry() {
    }

    public MergeEntry(String id, @Nullable String icon, int count, @Nullable List<String> items, @Nullable Map<String, String> names, @Nullable Path sourceDir) {
        this.id = id;
        this.icon = icon == null ? "" : icon;
        this.count = Math.max(1, Math.min(9, count));
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        this.names = names == null ? new LinkedHashMap<>() : new LinkedHashMap<>(names);
        this.sourceDir = sourceDir;
    }

    public boolean hasIconReference() {
        return !icon.isBlank();
    }

    @Nullable
    public String displayName(@Nullable String languageCode) {
        if (names.isEmpty()) {
            return null;
        }
        if (languageCode != null && names.containsKey(languageCode)) {
            return names.get(languageCode);
        }
        String base = languageCode == null ? null : languageCode.split("_", 2)[0];
        if (base != null) {
            for (Map.Entry<String, String> entry : names.entrySet()) {
                if (entry.getKey().equals(base) || entry.getKey().startsWith(base + "_")) {
                    return entry.getValue();
                }
            }
        }
        if (names.containsKey("en_us")) {
            return names.get("en_us");
        }
        return names.values().iterator().next();
    }

    @Nullable
    public Path resolveIcon(Path root) {
        if (hasIconReference()) {
            if (sourceDir != null) {
                Path candidate = sourceDir.resolve(icon).normalize();
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
            Path fromRoot = root.resolve(icon).normalize();
            return Files.isRegularFile(fromRoot) ? fromRoot : null;
        }
        if (sourceDir != null) {
            Path sibling = sourceDir.resolve(id.substring(id.indexOf('/') + 1) + ".png");
            if (Files.isRegularFile(sibling)) {
                return sibling;
            }
        }
        return null;
    }
}
