package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服务端 -> 客户端：打开角色状态面板。
 */
public record OpenStatusPayload(
        boolean hasLingGen,
        double impurity,
        Map<String, Double> roots,
        boolean physiqueResolved,
        String physiqueId,
        List<String> traitIds
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenStatusPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "open_status"));

    public static final StreamCodec<ByteBuf, OpenStatusPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            OpenStatusPayload::hasLingGen,
            ByteBufCodecs.DOUBLE,
            OpenStatusPayload::impurity,
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
            OpenStatusPayload::roots,
            ByteBufCodecs.BOOL,
            OpenStatusPayload::physiqueResolved,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            payload -> Optional.ofNullable(payload.physiqueId),
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            OpenStatusPayload::traitIds,
            (hasLingGen, impurity, roots, physiqueResolved, optPhysiqueId, traitIds) ->
                    new OpenStatusPayload(hasLingGen, impurity, roots, physiqueResolved, optPhysiqueId.orElse(null), traitIds)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}