package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：请求抽取灵根。
 */
public record RequestLingGenDrawPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestLingGenDrawPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "request_lingen_draw"));

    public static final StreamCodec<ByteBuf, RequestLingGenDrawPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new RequestLingGenDrawPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
