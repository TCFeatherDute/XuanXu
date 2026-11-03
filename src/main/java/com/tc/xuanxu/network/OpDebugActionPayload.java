package com.tc.xuanxu.network;

import com.tc.xuanxu.XuanXuMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：OP 调试面板的操作请求。
 * <p>
 * 服务端会再次校验 OP 权限后才执行，客户端不可信。
 */
public record OpDebugActionPayload(int action) implements CustomPacketPayload {
    /** 重新随机灵根（直接覆盖当前灵根数据）。 */
    public static final int ACTION_REROLL_LINGEN = 0;
    /** 重新随机灵体（直接覆盖当前灵体）。 */
    public static final int ACTION_REROLL_PHYSIQUE = 1;
    /** 重新随机 4 个天命词条（直接覆盖当前词条）。 */
    public static final int ACTION_REROLL_TRAIT = 2;
    /** 清空灵根 / 灵体 / 词条全部觉醒数据，回到未觉醒状态。 */
    public static final int ACTION_RESET_ALL = 3;

    public static final CustomPacketPayload.Type<OpDebugActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(XuanXuMod.MOD_ID, "op_debug_action"));

    public static final StreamCodec<ByteBuf, OpDebugActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OpDebugActionPayload::action,
            OpDebugActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}