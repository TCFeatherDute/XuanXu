package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：刷新词条。
 */
public record TraitRerollPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TraitRerollPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "trait_reroll"));

    public static final StreamCodec<ByteBuf, TraitRerollPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new TraitRerollPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
