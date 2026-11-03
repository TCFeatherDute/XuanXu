package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：固定当前第一个未固定的已选词条。
 */
public record TraitFixPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TraitFixPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "trait_fix"));

    public static final StreamCodec<ByteBuf, TraitFixPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new TraitFixPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
