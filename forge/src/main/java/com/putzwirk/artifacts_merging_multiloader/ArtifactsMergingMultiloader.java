package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.client.ForgeClientHooks;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncPayload;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Constants.MOD_ID)
public class ArtifactsMergingMultiloader {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);

    private static final SimpleChannel CHANNEL = ChannelBuilder
        .named(Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH))
        .networkProtocolVersion(1)
        .optional()
        .simpleChannel();

    public ArtifactsMergingMultiloader(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ITEMS.register(ModItems.RANDOM_ARTIFACT_NAME, ModItems::create);
        RECIPE_SERIALIZERS.register(ModRecipes.ARTIFACT_MERGING_NAME, ModRecipes::create);
        ITEMS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);

        CHANNEL.messageBuilder(ConfigSyncPayload.class, 0)
            .encoder((message, buffer) -> buffer.writeUtf(message.payload(), Constants.CONFIG_PAYLOAD_MAX_CHARS))
            .decoder(buffer -> new ConfigSyncPayload(buffer.readUtf(Constants.CONFIG_PAYLOAD_MAX_CHARS)))
            .consumerMainThread((message, ctx) -> ClientConfigSync.apply(message.payload()))
            .add();

        ArtifactsMergingCommon.init();

        MinecraftForge.EVENT_BUS.addListener(ArtifactsMergingMultiloader::onPlayerLogin);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientHooks.init(context);
        }
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!CHANNEL.isRemotePresent(player.connection.getConnection())) {
            return;
        }
        CHANNEL.send(new ConfigSyncPayload(MergeConfigManager.exportJson()),
            PacketDistributor.PLAYER.with(player));
    }
}
