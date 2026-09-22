package com.putzwirk.artifacts_merging_multiloader.network;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigSyncPayload(String payload) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE =
        new Type<>(Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.stringUtf8(Constants.CONFIG_PAYLOAD_MAX_CHARS),
            ConfigSyncPayload::payload,
            ConfigSyncPayload::new);

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }
}
