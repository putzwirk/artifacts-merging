package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.client.ForgeClientHooks;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncMessage;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@Mod(Constants.MOD_ID)
public class ArtifactsMergingMultiloader {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);

    private static final ResourceLocation CONFIG_CHANNEL = Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH);
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        CONFIG_CHANNEL,
        () -> "1",
        NetworkRegistry.acceptMissingOr("1"),
        NetworkRegistry.acceptMissingOr("1"));

    public ArtifactsMergingMultiloader() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(ModItems.RANDOM_ARTIFACT_NAME, ModItems::create);
        RECIPE_SERIALIZERS.register(ModRecipes.ARTIFACT_MERGING_NAME, ModRecipes::create);
        ITEMS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);

        CHANNEL.registerMessage(0, ConfigSyncMessage.class,
            (message, buffer) -> buffer.writeUtf(message.payload, Constants.CONFIG_PAYLOAD_MAX_CHARS),
            buffer -> new ConfigSyncMessage(buffer.readUtf(Constants.CONFIG_PAYLOAD_MAX_CHARS)),
            (message, context) -> {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    context.get().enqueueWork(() -> ClientConfigSync.apply(message.payload));
                }
                context.get().setPacketHandled(true);
            },
            Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        ArtifactsMergingCommon.init();

        MinecraftForge.EVENT_BUS.addListener(ArtifactsMergingMultiloader::onPlayerLogin);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientHooks.init();
        }
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!CHANNEL.isRemotePresent(player.connection.connection)) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new ConfigSyncMessage(MergeConfigManager.exportJson()));
    }
}
