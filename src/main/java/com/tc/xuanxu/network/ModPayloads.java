package com.tc.xuanxu.network;

import com.tc.xuanxu.client.ClientPayloadHandler;
import com.tc.xuanxu.lingen.LingGenData;
import com.tc.xuanxu.lingen.LingGenManager;
import com.tc.xuanxu.lingen.LingGenRootRegistry;
import com.tc.xuanxu.lingen.LingGenType;
import com.tc.xuanxu.physique.PhysiqueData;
import com.tc.xuanxu.physique.PhysiqueManager;
import com.tc.xuanxu.physique.PhysiqueRegistry;
import com.tc.xuanxu.physique.PhysiqueType;
import com.tc.xuanxu.trait.TraitData;
import com.tc.xuanxu.trait.TraitDefinition;
import com.tc.xuanxu.trait.TraitManager;
import com.tc.xuanxu.trait.TraitRegistry;
import com.tc.xuanxu.trait.TraitSelectionData;
import com.tc.xuanxu.util.OpPanelAccess;
import com.tc.xuanxu.util.RandomUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 模组网络包注册。
 */
public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                OpenLingGenScreenPayload.TYPE,
                OpenLingGenScreenPayload.STREAM_CODEC,
                ModPayloads::handleOpenLingGenScreen
        );

        registrar.playToClient(
                OpenPhysiqueSelectPayload.TYPE,
                OpenPhysiqueSelectPayload.STREAM_CODEC,
                ModPayloads::handleOpenPhysiqueSelect
        );

        registrar.playToServer(
                RequestLingGenDrawPayload.TYPE,
                RequestLingGenDrawPayload.STREAM_CODEC,
                ModPayloads::handleRequestLingGenDraw
        );

        registrar.playToServer(
                RequestPhysiqueSelectPayload.TYPE,
                RequestPhysiqueSelectPayload.STREAM_CODEC,
                ModPayloads::handleRequestPhysiqueSelect
        );

        registrar.playToServer(
                ChoosePhysiquePayload.TYPE,
                ChoosePhysiquePayload.STREAM_CODEC,
                ModPayloads::handleChoosePhysique
        );

        registrar.playToClient(
                OpenTraitSelectPayload.TYPE,
                OpenTraitSelectPayload.STREAM_CODEC,
                ModPayloads::handleOpenTraitSelect
        );

        registrar.playToClient(
                CloseTraitScreenPayload.TYPE,
                CloseTraitScreenPayload.STREAM_CODEC,
                ModPayloads::handleCloseTraitScreen
        );

        registrar.playToServer(
                TraitChoosePayload.TYPE,
                TraitChoosePayload.STREAM_CODEC,
                ModPayloads::handleTraitChoose
        );

        registrar.playToServer(
                TraitFixPayload.TYPE,
                TraitFixPayload.STREAM_CODEC,
                ModPayloads::handleTraitFix
        );

        registrar.playToServer(
                TraitRerollPayload.TYPE,
                TraitRerollPayload.STREAM_CODEC,
                ModPayloads::handleTraitReroll
        );

        registrar.playToServer(
                TraitConfirmPayload.TYPE,
                TraitConfirmPayload.STREAM_CODEC,
                ModPayloads::handleTraitConfirm
        );

        registrar.playToServer(
                RequestStatusPayload.TYPE,
                RequestStatusPayload.STREAM_CODEC,
                ModPayloads::handleRequestStatus
        );

        registrar.playToClient(
                OpenStatusPayload.TYPE,
                OpenStatusPayload.STREAM_CODEC,
                ModPayloads::handleOpenStatus
        );

        // OP 调试面板
        registrar.playToClient(
                OpenOpDebugPayload.TYPE,
                OpenOpDebugPayload.STREAM_CODEC,
                ModPayloads::handleOpenOpDebug
        );

        registrar.playToServer(
                OpDebugActionPayload.TYPE,
                OpDebugActionPayload.STREAM_CODEC,
                ModPayloads::handleOpDebugAction
        );
    }

    private static void handleOpenLingGenScreen(OpenLingGenScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.openLingGenScreen(payload);
            }
        });
    }

    private static void handleOpenPhysiqueSelect(OpenPhysiqueSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.openPhysiqueSelect(payload);
            }
        });
    }

    private static void handleRequestLingGenDraw(RequestLingGenDrawPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // 一次性守卫：已拥有灵根时禁止通过网络包无限重抽（重抽请走 OP 指令）
                if (LingGenManager.get(player) != null) {
                    player.sendSystemMessage(Component.literal("你已觉醒灵根，无法再次抽取。"));
                    return;
                }
                LingGenData data = LingGenManager.generate(player.getRandom());
                LingGenManager.set(player, data);
                PacketDistributor.sendToPlayer(player, toLingGenPayload(data));
            }
        });
    }

    private static void handleRequestPhysiqueSelect(RequestPhysiqueSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // 一次性守卫：已觉醒灵体时禁止重抽（重抽请走 OP 指令）
                if (PhysiqueManager.isResolved(player)) {
                    player.sendSystemMessage(Component.literal("你已完成灵体觉醒，无法再次抽取。"));
                    return;
                }
                List<PhysiqueType> options = PhysiqueManager.getPendingOptions(player);
                if (!options.isEmpty()) {
                    // 已有待选项时直接展示，避免反复抽卡
                    List<String> ids = options.stream().map(PhysiqueType::id).toList();
                    PacketDistributor.sendToPlayer(player, new OpenPhysiqueSelectPayload(ids));
                    return;
                }
                options = PhysiqueManager.rollOptions(player.getRandom());
                PhysiqueManager.setPendingOptions(player, options);
                List<String> ids = options.stream().map(PhysiqueType::id).toList();
                PacketDistributor.sendToPlayer(player, new OpenPhysiqueSelectPayload(ids));
            }
        });
    }

    private static void handleChoosePhysique(ChoosePhysiquePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // 一次性守卫：已觉醒灵体时忽略重复选择
                if (PhysiqueManager.isResolved(player)) {
                    return;
                }
                List<PhysiqueType> pending = PhysiqueManager.getPendingOptions(player);
                if (payload.optionIndex() >= 0 && payload.optionIndex() < pending.size()) {
                    PhysiqueManager.setCurrent(player, pending.get(payload.optionIndex()));
                    player.sendSystemMessage(Component.literal("灵体选择完成！开始天命词条抽取。"));
                    TraitManager.startSelection(player);
                    sendTraitSelect(player);
                }
            }
        });
    }

    private static void handleOpenTraitSelect(OpenTraitSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.openTraitSelect(payload);
            }
        });
    }

    private static void handleCloseTraitScreen(CloseTraitScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.closeTraitScreen(payload);
            }
        });
    }

    private static void handleTraitChoose(TraitChoosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TraitSelectionData data = TraitManager.getPending(player);
                if (data == null) {
                    return;
                }
                int index = payload.optionIndex();
                if (index >= 0 && index < data.getOptions().size()) {
                    String id = data.getOptions().get(index);
                    if (data.getSelected().contains(id)) {
                        TraitManager.deselectOption(player, index);
                    } else {
                        TraitManager.selectOption(player, index);
                    }
                }
                sendTraitSelect(player);
            }
        });
    }

    private static void handleTraitFix(TraitFixPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TraitManager.fixFirstSelected(player);
                sendTraitSelect(player);
            }
        });
    }

    private static void handleTraitReroll(TraitRerollPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TraitManager.reroll(player);
                sendTraitSelect(player);
            }
        });
    }

    private static void handleTraitConfirm(TraitConfirmPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (TraitManager.confirm(player)) {
                    player.sendSystemMessage(Component.literal("天命词条确认完成！"));
                    PacketDistributor.sendToPlayer(player, new CloseTraitScreenPayload());
                } else {
                    player.sendSystemMessage(Component.literal("请恰好选择 4 个词条后再确认。"));
                    sendTraitSelect(player);
                }
            }
        });
    }

    private static void handleRequestStatus(RequestStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                LingGenData lingen = LingGenManager.get(player);
                PhysiqueData physique = PhysiqueManager.getData(player);
                TraitData traits = TraitManager.getData(player);

                PacketDistributor.sendToPlayer(player, new OpenStatusPayload(
                        lingen != null,
                        lingen != null ? lingen.getImpurity() : 0.0,
                        lingen != null ? new LinkedHashMap<>(lingen.getRoots()) : new LinkedHashMap<>(),
                        physique.isResolved(),
                        physique.getPhysiqueId(),
                        traits.getTraitIds()
                ));
            }
        });
    }

    private static void handleOpenStatus(OpenStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.openStatusScreen(payload);
            }
        });
    }

    // ==================== OP 调试面板 ====================

    private static void handleOpenOpDebug(OpenOpDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLLoader.getDist().isClient()) {
                ClientPayloadHandler.openOpDebugScreen(payload);
            }
        });
    }

    /**
     * OP 调试操作：服务端二次校验权限（客户端不可信），并绕过一次性抽取守卫，
     * 从而支持在创造模式下无限刷新灵根 / 灵体 / 词条。
     */
    private static void handleOpDebugAction(OpDebugActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!OpPanelAccess.allowed(player)) {
                player.sendSystemMessage(Component.literal("此功能仅限管理员（OP）或在单人创造模式下使用。")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            RandomSource random = player.getRandom();
            switch (payload.action()) {
                case OpDebugActionPayload.ACTION_REROLL_LINGEN -> {
                    LingGenManager.set(player, LingGenManager.generate(random));
                    // 用动作栏提示而非聊天消息，避免连续刷新时刷屏
                    actionBar(player, Component.literal("已重新随机灵根").withStyle(ChatFormatting.GREEN));
                }
                case OpDebugActionPayload.ACTION_REROLL_PHYSIQUE -> {
                    List<PhysiqueType> pool = new ArrayList<>(PhysiqueRegistry.all());
                    if (pool.isEmpty()) {
                        actionBar(player, Component.literal("没有可用的灵体配置").withStyle(ChatFormatting.RED));
                    } else {
                        PhysiqueType picked = pool.get(random.nextInt(pool.size()));
                        PhysiqueManager.setCurrent(player, picked);
                        actionBar(player, Component.literal("已重新随机灵体：" + picked.name()
                                        + " [" + picked.rarity().getDisplayName() + "]")
                                .withStyle(style -> style.withColor(picked.rarity().getColor())));
                    }
                }
                case OpDebugActionPayload.ACTION_REROLL_TRAIT -> {
                    List<String> rolled = rollRandomTraits(random);
                    if (rolled.isEmpty()) {
                        actionBar(player, Component.literal("没有可用的词条配置").withStyle(ChatFormatting.RED));
                    } else {
                        TraitManager.saveData(player, new TraitData(rolled));
                        TraitManager.clearPending(player);
                        actionBar(player, Component.literal("已重新随机 " + rolled.size() + " 个天命词条")
                                .withStyle(ChatFormatting.GREEN));
                    }
                }
                case OpDebugActionPayload.ACTION_RESET_ALL -> {
                    LingGenManager.reset(player);
                    PhysiqueManager.reset(player);
                    TraitManager.reset(player);
                    player.sendSystemMessage(Component.literal(
                                    "已重置灵根 / 灵体 / 词条数据，现在重新开始觉醒流程。")
                            .withStyle(ChatFormatting.YELLOW));
                    // 重置后立即重新触发觉醒流程，直接弹出灵根抽取界面
                    Map<String, Double> emptyRoots = new LinkedHashMap<>();
                    for (LingGenType type : LingGenRootRegistry.all()) {
                        emptyRoots.put(type.id(), 0.0);
                    }
                    PacketDistributor.sendToPlayer(player,
                            new OpenLingGenScreenPayload(false, 0.0, emptyRoots, 1.0, true));
                    return;
                }
                default -> {
                    return;
                }
            }
            // 回发最新状态以刷新面板显示
            sendOpDebugState(player);
        });
    }

    /**
     * 在动作栏（物品栏上方）显示提示，避免连续刷新时刷屏聊天栏。
     */
    private static void actionBar(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }

    /**
     * 随机抽取最多 MAX_TRAITS 个词条，并跳过冲突组重复的词条。
     */
    private static List<String> rollRandomTraits(RandomSource random) {
        List<TraitDefinition> pool = new ArrayList<>(TraitRegistry.enabledTraits());
        RandomUtils.shuffle(pool, random);
        List<String> ids = new ArrayList<>();
        Set<String> usedGroups = new HashSet<>();
        for (TraitDefinition trait : pool) {
            if (ids.size() >= TraitManager.MAX_TRAITS) {
                break;
            }
            if (!trait.conflictGroup().isEmpty() && !usedGroups.add(trait.conflictGroup())) {
                continue;
            }
            ids.add(trait.id());
        }
        return ids;
    }

    /**
     * 向玩家发送当前觉醒状态并打开 OP 调试面板。
     */
    public static void sendOpDebugState(ServerPlayer player) {
        LingGenData lingen = LingGenManager.get(player);
        PhysiqueData physique = PhysiqueManager.getData(player);
        TraitData traits = TraitManager.getData(player);

        PacketDistributor.sendToPlayer(player, new OpenOpDebugPayload(
                lingen != null ? new LinkedHashMap<>(lingen.getRoots()) : new LinkedHashMap<>(),
                lingen != null ? lingen.getImpurity() : 0.0,
                Optional.ofNullable(physique.getPhysiqueId()),
                traits.getTraitIds()
        ));
    }

    public static void sendTraitSelect(ServerPlayer player) {
        TraitSelectionData data = TraitManager.getPending(player);
        if (data == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new OpenTraitSelectPayload(
                data.getOptions(),
                data.getSelected(),
                Optional.ofNullable(data.getFixedId()),
                data.getRerollsLeft()
        ));
    }

    private static OpenLingGenScreenPayload toLingGenPayload(LingGenData data) {
        Map<String, Double> roots = new LinkedHashMap<>(data.getRoots());
        return new OpenLingGenScreenPayload(
                true,
                data.getImpurity(),
                roots,
                data.getCultivationSpeedMultiplier(),
                true
        );
    }
}
