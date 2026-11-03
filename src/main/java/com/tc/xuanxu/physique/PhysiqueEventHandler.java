package com.tc.xuanxu.physique;

import com.tc.xuanxu.lingen.LingGenManager;
import com.tc.xuanxu.network.ModPayloads;
import com.tc.xuanxu.network.OpenPhysiqueSelectPayload;
import com.tc.xuanxu.trait.TraitManager;
import com.tc.xuanxu.trait.TraitSelectionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 觉醒流程事件：按顺序推进 灵根 → 灵体 → 天命词条 抽取。
 * <p>
 * 流程未完成时登录会自动弹出对应 GUI，且抽取界面禁止 ESC 退出（见 FlowLockScreen），
 * 保证玩家必须完成全部觉醒流程。
 */
public final class PhysiqueEventHandler {
    private PhysiqueEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 如果玩家还没有灵根数据，说明还在灵根抽取阶段，不提前弹灵体
        if (LingGenManager.get(player) == null) {
            return;
        }

        if (PhysiqueManager.isResolved(player)) {
            // 灵体已选择：若天命词条尚未确认，恢复词条选择 GUI
            TraitSelectionData traitPending = TraitManager.getPending(player);
            if (traitPending != null) {
                ModPayloads.sendTraitSelect(player);
            }
            return;
        }

        List<PhysiqueType> pending = PhysiqueManager.getPendingOptions(player);
        if (pending.isEmpty()) {
            pending = PhysiqueManager.rollOptions(player.getRandom());
            PhysiqueManager.setPendingOptions(player, pending);
        }

        if (pending.isEmpty()) {
            // 注册表为空等极端情况，直接提示，不弹空 GUI
            player.sendSystemMessage(Component.literal("【玄墟】当前没有可用的灵体选项。").withStyle(ChatFormatting.RED));
            return;
        }

        // 自动弹出灵体选择 GUI
        List<String> ids = pending.stream().map(PhysiqueType::id).toList();
        PacketDistributor.sendToPlayer(player, new OpenPhysiqueSelectPayload(ids));

        // 同时聊天提示，以备 GUI 无法正常显示时使用命令
        player.sendSystemMessage(Component.literal("【玄墟】灵体觉醒！请选择你的灵体：")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("使用 /cultivation physique choose <1|2|3> 选择，或 /cultivation physique choose none 放弃成为凡体。")
                .withStyle(ChatFormatting.GRAY));
    }

    /**
     * 以聊天消息形式列出灵体选项（命令输出用）。
     */
    public static void sendOptions(ServerPlayer player, List<PhysiqueType> options) {
        for (int i = 0; i < options.size(); i++) {
            PhysiqueType type = options.get(i);
            player.sendSystemMessage(Component.literal((i + 1) + ". " + type.name()
                            + " [" + type.rarity().getDisplayName() + "]")
                    .withStyle(style -> style.withColor(type.rarity().getColor())));
            player.sendSystemMessage(Component.literal("   " + type.description()).withStyle(ChatFormatting.GRAY));
        }
    }
}