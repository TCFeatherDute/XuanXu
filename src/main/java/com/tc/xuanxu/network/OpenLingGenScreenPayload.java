package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务端 -> 客户端：打开灵根面板。
 * hasData 为 false 时表示尚未抽取灵根，客户端显示抽取按钮。
 * forced 为 true 时表示处于强制觉醒流程中（禁止 ESC 退出），
 * 例如登录弹出的抽取界面；为 false 时是查看模式（如命令打开），可正常关闭。
 */
public record OpenLingGenScreenPayload(boolean hasData, double impurity, Map<String, Double> roots, double cultivationMultiplier, boolean forced) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenLingGenScreenPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "open_lingen_screen"));

    public static final StreamCodec<ByteBuf, OpenLingGenScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            OpenLingGenScreenPayload::hasData,
            ByteBufCodecs.DOUBLE,
            OpenLingGenScreenPayload::impurity,
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
            OpenLingGenScreenPayload::roots,
            ByteBufCodecs.DOUBLE,
            OpenLingGenScreenPayload::cultivationMultiplier,
            ByteBufCodecs.BOOL,
            OpenLingGenScreenPayload::forced,
            OpenLingGenScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}