package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：选择/取消选择某个词条。
 */
public record TraitChoosePayload(int optionIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TraitChoosePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "trait_choose"));

    public static final StreamCodec<ByteBuf, TraitChoosePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TraitChoosePayload::optionIndex,
            TraitChoosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
