package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class Ids {
    private Ids() {
    }

    public static ResourceLocation of(String namespace, String path) {
        return ResourceLocation.tryBuild(namespace, path);
    }

    @Nullable
    public static ResourceLocation parse(String id) {
        return ResourceLocation.tryParse(id);
    }
}
