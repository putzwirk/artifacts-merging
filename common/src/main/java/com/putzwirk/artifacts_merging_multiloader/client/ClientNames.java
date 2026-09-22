package com.putzwirk.artifacts_merging_multiloader.client;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;

public final class ClientNames {
    private ClientNames() {
    }

    @Nullable
    public static String displayName(String groupId) {
        MergeEntry entry = MergeConfigManager.byId(groupId);
        if (entry == null) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        String language = minecraft == null ? null : minecraft.options.languageCode;
        return entry.displayName(language);
    }
}
