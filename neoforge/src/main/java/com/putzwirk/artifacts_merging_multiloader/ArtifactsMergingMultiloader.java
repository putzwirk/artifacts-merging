package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.client.NeoForgeClientHooks;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncPayload;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class ArtifactsMergingMultiloader {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public ArtifactsMergingMultiloader(IEventBus modBus, ModContainer container) {
        ITEMS.register(ModItems.RANDOM_ARTIFACT_NAME, ModItems::create);
        RECIPE_SERIALIZERS.register(ModRecipes.ARTIFACT_MERGING_NAME, ModRecipes::create);
        ITEMS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);

        modBus.addListener(this::registerPayloads);

        ArtifactsMergingCommon.init();

        NeoForge.EVENT_BUS.addListener(ArtifactsMergingMultiloader::onPlayerLogin);
        if (FMLEnvironment.getDist().isClient()) {
            container.registerExtensionPoint(IConfigScreenFactory.class, NeoForgeClientHooks.configScreen());
            NeoForge.EVENT_BUS.addListener(NeoForgeClientHooks::onLoggingOut);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientConfigSync.apply(payload.json())));
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(MergeConfigManager.exportJson()));
        }
    }
}
