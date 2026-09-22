package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ArtifactsMergingMultiloaderClient implements ClientModInitializer {
    private static final ResourceLocation CONFIG_CHANNEL = Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH);

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CONFIG_CHANNEL, (client, listener, buffer, sender) -> {
            String payload = buffer.readUtf(Constants.CONFIG_PAYLOAD_MAX_CHARS);
            client.execute(() -> ClientConfigSync.apply(payload));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) ->
            client.execute(ClientConfigSync::resetToLocal));
    }
}
