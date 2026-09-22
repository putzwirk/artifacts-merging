package com.putzwirk.artifacts_merging_multiloader.client;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigScreen;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeClientHooks {
    private NeoForgeClientHooks() {
    }

    public static IConfigScreenFactory configScreen() {
        return (container, parent) -> MergeConfigScreen.create(parent);
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientConfigSync.resetToLocal();
    }
}
