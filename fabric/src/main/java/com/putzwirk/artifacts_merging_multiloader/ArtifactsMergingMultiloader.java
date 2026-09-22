package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ArtifactsMergingMultiloader implements ModInitializer {
    private static final ResourceLocation CONFIG_CHANNEL = Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH);

    @Override
    public void onInitialize() {
        ArtifactsMergingCommon.init();
        Registry.register(BuiltInRegistries.ITEM, ModItems.RANDOM_ARTIFACT_ID, ModItems.create());
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ModRecipes.ARTIFACT_MERGING_ID, ModRecipes.create());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendConfig(handler.player));
    }

    private static void sendConfig(ServerPlayer player) {
        if (!ServerPlayNetworking.canSend(player, CONFIG_CHANNEL)) {
            return;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeUtf(MergeConfigManager.exportJson(), Constants.CONFIG_PAYLOAD_MAX_CHARS);
        ServerPlayNetworking.send(player, CONFIG_CHANNEL, buffer);
    }
}
