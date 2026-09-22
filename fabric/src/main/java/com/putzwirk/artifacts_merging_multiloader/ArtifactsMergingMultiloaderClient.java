package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.client.ClientConfigSync;
import com.putzwirk.artifacts_merging_multiloader.network.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ArtifactsMergingMultiloaderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
            context.client().execute(() -> ClientConfigSync.apply(payload.payload())));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) ->
            client.execute(ClientConfigSync::resetToLocal));
    }
}
