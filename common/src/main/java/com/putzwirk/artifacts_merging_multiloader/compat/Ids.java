package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

public final class Ids {
    private Ids() {
    }

    public static Identifier of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Nullable
    public static Identifier parse(String id) {
        return Identifier.tryParse(id);
    }
}
