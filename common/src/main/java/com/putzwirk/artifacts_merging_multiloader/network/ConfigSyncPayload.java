package com.putzwirk.artifacts_merging_multiloader.network;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigSyncPayload(String json) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigSyncPayload> ID =
        new CustomPacketPayload.Type<>(Ids.of(Constants.MOD_ID, Constants.CONFIG_CHANNEL_PATH));

    public static final StreamCodec<ByteBuf, ConfigSyncPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(Constants.CONFIG_PAYLOAD_MAX_CHARS),
        ConfigSyncPayload::json,
        ConfigSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
