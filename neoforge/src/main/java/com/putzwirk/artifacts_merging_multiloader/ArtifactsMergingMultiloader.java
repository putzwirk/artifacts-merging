package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncPayload;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class ArtifactsMergingMultiloader {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public ArtifactsMergingMultiloader(IEventBus modBus) {
        ITEMS.register(ModItems.RANDOM_ARTIFACT_NAME, ModItems::create);
        RECIPE_SERIALIZERS.register(ModRecipes.ARTIFACT_MERGING_NAME, ModRecipes::create);
        ITEMS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(ArtifactsMergingMultiloader::registerPayloads);

        ArtifactsMergingCommon.init();

        NeoForge.EVENT_BUS.addListener(ArtifactsMergingMultiloader::onPlayerLogin);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(
            ConfigSyncPayload.TYPE,
            ConfigSyncPayload.STREAM_CODEC,
            (payload, context) -> {
                if (FMLEnvironment.getDist() == Dist.CLIENT) {
                    context.enqueueWork(() -> ClientConfigSync.apply(payload.payload()));
                }
            });
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.connection.hasChannel(ConfigSyncPayload.TYPE)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(MergeConfigManager.exportJson()));
    }
}
