package com.putzwirk.artifacts_merging_multiloader.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientIconCache {
    public record Icon(ResourceLocation location, int width, int height) {
    }

    private static final Map<String, Icon> CACHE = new ConcurrentHashMap<>();
    private static volatile boolean dirty = true;

    private ClientIconCache() {
    }

    public static void invalidate() {
        dirty = true;
    }

    public static boolean hasIcon(String groupId) {
        ensureBuilt();
        return CACHE.containsKey(groupId);
    }

    @Nullable
    public static Icon icon(String groupId) {
        ensureBuilt();
        return CACHE.get(groupId);
    }

    private static void ensureBuilt() {
        if (!dirty) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !minecraft.isSameThread()) {
            return;
        }
        rebuild(minecraft);
        dirty = false;
    }

    private static void rebuild(Minecraft minecraft) {
        for (Icon icon : CACHE.values()) {
            minecraft.getTextureManager().release(icon.location());
        }
        CACHE.clear();
        for (MergeEntry entry : MergeConfigManager.groups()) {
            NativeImage image = readImage(entry);
            if (image == null) {
                continue;
            }
            DynamicTexture texture = new DynamicTexture(() -> "artifactsmerging " + entry.id, image);
            String safe = entry.id.toLowerCase().replaceAll("[^a-z0-9_./-]", "_");
            ResourceLocation location = Ids.of("artifactsmerging", safe);
            minecraft.getTextureManager().register(location, texture);
            CACHE.put(entry.id, new Icon(location, image.getWidth(), image.getHeight()));
        }
    }

    @Nullable
    private static NativeImage readImage(MergeEntry entry) {
        try {
            if (entry.iconData != null) {
                try (InputStream stream = new ByteArrayInputStream(entry.iconData)) {
                    return NativeImage.read(stream);
                }
            }
            Path root = MergeConfigManager.root();
            if (root == null) {
                return null;
            }
            Path file = entry.resolveIcon(root);
            if (file == null) {
                return null;
            }
            try (InputStream stream = Files.newInputStream(file)) {
                return NativeImage.read(stream);
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
