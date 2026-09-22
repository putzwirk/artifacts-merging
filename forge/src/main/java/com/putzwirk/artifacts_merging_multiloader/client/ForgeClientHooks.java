package com.putzwirk.artifacts_merging_multiloader.client;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

public final class ForgeClientHooks {
    private ForgeClientHooks() {
    }

    public static void init(ModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> MergeConfigScreen.create(parent)));
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ClientConfigSync.resetToLocal());
    }
}
