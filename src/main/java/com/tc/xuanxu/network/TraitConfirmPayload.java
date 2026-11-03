package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：确认最终词条。
 */
public record TraitConfirmPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TraitConfirmPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "trait_confirm"));

    public static final StreamCodec<ByteBuf, TraitConfirmPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new TraitConfirmPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
