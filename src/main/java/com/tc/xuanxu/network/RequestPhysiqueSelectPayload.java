package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：请求进入灵体抽取界面。
 */
public record RequestPhysiqueSelectPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestPhysiqueSelectPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "request_physique_select"));

    public static final StreamCodec<ByteBuf, RequestPhysiqueSelectPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {}, buffer -> new RequestPhysiqueSelectPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
