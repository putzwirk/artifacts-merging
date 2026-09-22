package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncPayload;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

public class ArtifactsMergingMultiloader implements ModInitializer {

    @Override
    public void onInitialize() {
        ArtifactsMergingCommon.init();
        Registry.register(BuiltInRegistries.ITEM, ModItems.RANDOM_ARTIFACT_ID, ModItems.create());
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ModRecipes.ARTIFACT_MERGING_ID, ModRecipes.create());
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendConfig(handler.player));
    }

    private static void sendConfig(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, ConfigSyncPayload.TYPE)) {
            return;
        }
        ServerPlayNetworking.send(player, new ConfigSyncPayload(MergeConfigManager.exportJson()));
    }
}
