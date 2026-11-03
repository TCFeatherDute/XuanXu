package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 服务端 -> 客户端：打开词条十选四界面。
 */
public record OpenTraitSelectPayload(
        List<String> options,
        List<String> selected,
        Optional<String> fixedId,
        int rerollsLeft
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenTraitSelectPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "open_trait_select"));

    public static final StreamCodec<ByteBuf, OpenTraitSelectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            OpenTraitSelectPayload::options,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            OpenTraitSelectPayload::selected,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            OpenTraitSelectPayload::fixedId,
            ByteBufCodecs.VAR_INT,
            OpenTraitSelectPayload::rerollsLeft,
            OpenTraitSelectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
