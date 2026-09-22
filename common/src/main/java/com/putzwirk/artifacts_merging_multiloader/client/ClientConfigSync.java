package com.putzwirk.artifacts_merging_multiloader.client;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;

public final class ClientConfigSync {
    private ClientConfigSync() {
    }

    public static void apply(String payload) {
        MergeConfigManager.applyRemote(payload);
        ClientIconCache.invalidate();
    }

    public static void resetToLocal() {
        MergeConfigManager.reloadLocal();
        ClientIconCache.invalidate();
    }
}
