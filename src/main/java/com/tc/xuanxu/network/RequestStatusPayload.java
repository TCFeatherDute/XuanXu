package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：请求角色状态面板数据。
 */
public record RequestStatusPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestStatusPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "request_status"));

    public static final StreamCodec<ByteBuf, RequestStatusPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new RequestStatusPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}