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
 * 服务端 -> 客户端：打开 OP 调试面板并携带当前觉醒状态。
 * <p>
 * roots 为空表示尚未觉醒灵根；physiqueId 为空表示没有灵体；
 * traitIds 为当前已确认的天命词条。
 */
public record OpenOpDebugPayload(
        Map<String, Double> roots,
        double impurity,
        Optional<String> physiqueId,
        List<String> traitIds
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenOpDebugPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "open_op_debug"));

    public static final StreamCodec<ByteBuf, OpenOpDebugPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
            OpenOpDebugPayload::roots,
            ByteBufCodecs.DOUBLE,
            OpenOpDebugPayload::impurity,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            OpenOpDebugPayload::physiqueId,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            OpenOpDebugPayload::traitIds,
            OpenOpDebugPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}