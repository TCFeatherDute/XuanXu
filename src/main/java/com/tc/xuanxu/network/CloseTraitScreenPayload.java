package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 服务端 -> 客户端：关闭词条选择界面。
 */
public record CloseTraitScreenPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CloseTraitScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "close_trait_screen"));

    public static final StreamCodec<ByteBuf, CloseTraitScreenPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new CloseTraitScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
