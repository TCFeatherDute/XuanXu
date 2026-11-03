package com.tc.xuanxu.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tc.xuanxu.lingen.LingGenConfig;
import com.tc.xuanxu.lingen.LingGenData;
import com.tc.xuanxu.lingen.LingGenManager;
import com.tc.xuanxu.network.ModPayloads;
import com.tc.xuanxu.network.OpenLingGenScreenPayload;
import com.tc.xuanxu.physique.PhysiqueEventHandler;
import com.tc.xuanxu.physique.PhysiqueManager;
import com.tc.xuanxu.physique.PhysiqueType;
import com.tc.xuanxu.trait.TraitData;
import com.tc.xuanxu.trait.TraitDefinition;
import com.tc.xuanxu.trait.TraitManager;
import com.tc.xuanxu.trait.TraitPoolRegistry;
import com.tc.xuanxu.trait.TraitRegistry;
import com.tc.xuanxu.trait.TraitSelectionData;
import com.tc.xuanxu.util.OpPanelAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 玄墟模组统一指令入口。
 */
public final class CultivationCommands {
    private CultivationCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 灵根子命令
        var lingen = Commands.literal("lingen")
                .executes(CultivationCommands::showLingGen)
                .then(Commands.literal("reroll")
                        .requires(source -> source.hasPermission(2))
                        .executes(CultivationCommands::rerollLingGen));

        // 灵体子命令
        var physique = Commands.literal("physique")
                .executes(CultivationCommands::showPhysique)
                .then(Commands.literal("roll")
                        .requires(source -> source.hasPermission(2))
                        .executes(CultivationCommands::rollPhysique))
                .then(Commands.literal("choose")
                        .then(Commands.argument("option", IntegerArgumentType.integer(1, 3))
                                .executes(CultivationCommands::choosePhysique))
                        .then(Commands.literal("none")
                                .executes(CultivationCommands::chooseMortalPhysique)))
                .then(Commands.literal("trait")
                        .executes(CultivationCommands::showTraits)
                        .then(Commands.literal("roll")
                                .requires(source -> source.hasPermission(2))
                                .executes(CultivationCommands::rollTraits))
                        .then(Commands.literal("list")
                                .executes(CultivationCommands::listTraitOptions))
                        .then(Commands.literal("choose")
                                .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                        .executes(CultivationCommands::chooseTrait)))
                        .then(Commands.literal("fix")
                                .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                        .executes(CultivationCommands::fixTrait)))
                        .then(Commands.literal("reroll")
                                .requires(source -> source.hasPermission(2))
                                .executes(CultivationCommands::rerollTraits))
                        .then(Commands.literal("confirm")
                                .executes(CultivationCommands::confirmTraits)));

        // reload 子命令
        var reload = Commands.literal("reload")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("traits")
                        .executes(CultivationCommands::reloadTraits))
                .then(Commands.literal("pools")
                        .executes(CultivationCommands::reloadPools))
                .then(Commands.literal("lingen")
                        .executes(CultivationCommands::reloadLingGenConfig));

        // OP 调试面板（管理员，或单人世界创造模式）
        var op = Commands.literal("op")
                .requires(OpPanelAccess::allowed)
                .executes(CultivationCommands::openOpDebugPanel);

        // 注册主命令
        dispatcher.register(Commands.literal("cultivation")
                .then(lingen)
                .then(physique)
                .then(op)
                .then(reload));
    }

    /**
     * 打开 OP 调试面板：可在创造模式下无限刷新灵根 / 灵体 / 词条。
     */
    private static int openOpDebugPanel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!OpPanelAccess.allowed(player)) {
            player.sendSystemMessage(Component.literal("此功能仅限管理员（OP）或在单人创造模式下使用。")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ModPayloads.sendOpDebugState(player);
        return 1;
    }

    private static int reloadTraits(CommandContext<CommandSourceStack> context) {
        TraitRegistry.reload();
        context.getSource().sendSuccess(() -> Component.literal("词条已重新加载。").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int reloadPools(CommandContext<CommandSourceStack> context) {
        TraitPoolRegistry.reload();
        context.getSource().sendSuccess(() -> Component.literal("词条池已重新加载。").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int reloadLingGenConfig(CommandContext<CommandSourceStack> context) {
        LingGenConfig.reload();
        context.getSource().sendSuccess(() -> Component.literal("灵根配置已重新加载。").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int showLingGen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LingGenData data = LingGenManager.getOrCreate(player);

        Map<String, Double> roots = new LinkedHashMap<>(data.getRoots());
        OpenLingGenScreenPayload payload = new OpenLingGenScreenPayload(
                true,
                data.getImpurity(),
                roots,
                data.getCultivationSpeedMultiplier(),
                false
        );
        PacketDistributor.sendToPlayer(player, payload);
        player.sendSystemMessage(Component.literal("已打开灵根面板。").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int rerollLingGen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LingGenData data = LingGenManager.generate(player.getRandom());
        LingGenManager.set(player, data);
        player.sendSystemMessage(Component.literal("已重新觉醒灵根。").withStyle(ChatFormatting.GREEN));
        return showLingGen(context);
    }

    private static int showPhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!PhysiqueManager.isResolved(player)) {
            player.sendSystemMessage(Component.literal("尚未完成灵体觉醒。").withStyle(ChatFormatting.RED));
            List<PhysiqueType> pending = PhysiqueManager.getPendingOptions(player);
            if (!pending.isEmpty()) {
                PhysiqueEventHandler.sendOptions(player, pending);
            } else {
                player.sendSystemMessage(Component.literal("使用 /cultivation physique roll 生成选项。").withStyle(ChatFormatting.GRAY));
            }
            return 1;
        }

        PhysiqueType current = PhysiqueManager.getCurrent(player);
        player.sendSystemMessage(Component.literal("===== 玄墟 · 灵体面板 =====").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (current == null) {
            player.sendSystemMessage(Component.literal("当前灵体：凡体（无任何加成）"));
        } else {
            player.sendSystemMessage(Component.literal("当前灵体：" + current.name()
                    + " [" + current.rarity().getDisplayName() + "]")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(current.rarity().getColor()))));
            player.sendSystemMessage(Component.literal(current.description()).withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    private static int rollPhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<PhysiqueType> options = PhysiqueManager.rollOptions(player.getRandom());
        PhysiqueManager.setPendingOptions(player, options);
        player.sendSystemMessage(Component.literal("已生成新的灵体选项：").withStyle(ChatFormatting.LIGHT_PURPLE));
        PhysiqueEventHandler.sendOptions(player, options);
        player.sendSystemMessage(Component.literal("使用 /cultivation physique choose <1|2|3> 选择，或 /cultivation physique choose none 放弃成为凡体。")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int choosePhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int index = context.getArgument("option", Integer.class) - 1;
        List<PhysiqueType> pending = PhysiqueManager.getPendingOptions(player);

        if (pending.isEmpty()) {
            player.sendSystemMessage(Component.literal("当前没有待选择的灵体选项。").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (index < 0 || index >= pending.size()) {
            player.sendSystemMessage(Component.literal("无效的选项。").withStyle(ChatFormatting.RED));
            return 0;
        }

        PhysiqueType selected = pending.get(index);
        PhysiqueManager.setCurrent(player, selected);
        player.sendSystemMessage(Component.literal("你觉醒了灵体：" + selected.name()
                        + " [" + selected.rarity().getDisplayName() + "]")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        startTraitSelection(player);
        return 1;
    }

    private static int chooseMortalPhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (PhysiqueManager.getPendingOptions(player).isEmpty()) {
            player.sendSystemMessage(Component.literal("当前没有待选择的灵体选项。").withStyle(ChatFormatting.RED));
            return 0;
        }
        PhysiqueManager.setMortal(player);
        player.sendSystemMessage(Component.literal("你选择了凡体，没有获得任何灵体加成。").withStyle(ChatFormatting.GRAY));
        startTraitSelection(player);
        return 1;
    }

    /**
     * 灵体选择完成后启动天命词条抽取，与 GUI 路径保持一致。
     */
    private static void startTraitSelection(ServerPlayer player) {
        TraitManager.startSelection(player);
        ModPayloads.sendTraitSelect(player);
    }

    private static int showTraits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TraitData data = TraitManager.getData(player);

        player.sendSystemMessage(Component.literal("===== 玄墟 · 天命词条 =====").withStyle(ChatFormatting.GOLD));
        if (data.getTraitIds().isEmpty()) {
            player.sendSystemMessage(Component.literal("当前没有词条。").withStyle(ChatFormatting.GRAY));
        } else {
            for (String id : data.getTraitIds()) {
                TraitDefinition trait = TraitRegistry.get(id).orElse(null);
                if (trait != null) {
                    player.sendSystemMessage(Component.literal("- " + trait.name() + " [" + trait.rarity().getDisplayName() + "]")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(trait.rarity().getColor()))));
                    player.sendSystemMessage(Component.literal("  " + trait.description()).withStyle(ChatFormatting.GRAY));
                }
            }
        }
        return 1;
    }

    private static int rollTraits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TraitSelectionData data = TraitManager.startSelection(player);
        player.sendSystemMessage(Component.literal("已生成 10 个词条选项。").withStyle(ChatFormatting.GREEN));
        sendTraitOptions(player, data);
        return 1;
    }

    private static int listTraitOptions(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TraitSelectionData data = TraitManager.getPending(player);
        if (data == null) {
            player.sendSystemMessage(Component.literal("当前没有进行中的词条抽取，请先使用 /cultivation trait roll。").withStyle(ChatFormatting.RED));
            return 0;
        }
        sendTraitOptions(player, data);
        return 1;
    }

    private static int chooseTrait(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int index = context.getArgument("index", Integer.class) - 1;
        if (TraitManager.selectOption(player, index)) {
            player.sendSystemMessage(Component.literal("已选择词条。当前已选 " + TraitManager.getPending(player).getSelected().size() + "/4")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.literal("选择失败：可能已满 4 个、重复、互斥或选项无效。").withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int fixTrait(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int index = context.getArgument("index", Integer.class) - 1;
        if (TraitManager.fixOption(player, index)) {
            player.sendSystemMessage(Component.literal("已固定该词条，刷新时会保留。").withStyle(ChatFormatting.GREEN));
        } else {
            player.sendSystemMessage(Component.literal("固定失败：请先选择该词条，或选项无效。").withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int rerollTraits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (TraitManager.reroll(player)) {
            TraitSelectionData data = TraitManager.getPending(player);
            player.sendSystemMessage(Component.literal("刷新成功，剩余刷新次数：" + data.getRerollsLeft()).withStyle(ChatFormatting.GREEN));
            sendTraitOptions(player, data);
        } else {
            player.sendSystemMessage(Component.literal("刷新失败：没有进行中的抽取或刷新次数已用完。").withStyle(ChatFormatting.RED));
        }
        return 1;
    }

    private static int confirmTraits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (TraitManager.confirm(player)) {
            player.sendSystemMessage(Component.literal("词条确认完成！").withStyle(ChatFormatting.GREEN));
            return showTraits(context);
        } else {
            player.sendSystemMessage(Component.literal("确认失败：需要恰好选择 4 个词条。").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static void sendTraitOptions(ServerPlayer player, TraitSelectionData data) {
        player.sendSystemMessage(Component.literal("剩余刷新次数：" + data.getRerollsLeft()
                + "，已选：" + data.getSelected().size() + "/4"
                + (data.getFixedId() != null ? "，已固定：" + data.getFixedId() : "")).withStyle(ChatFormatting.YELLOW));
        List<String> options = data.getOptions();
        for (int i = 0; i < options.size(); i++) {
            TraitDefinition trait = TraitRegistry.get(options.get(i)).orElse(null);
            if (trait == null) {
                continue;
            }
            String selectedMark = data.getSelected().contains(trait.id()) ? " [已选]" : "";
            String fixedMark = trait.id().equals(data.getFixedId()) ? " [固定]" : "";
            player.sendSystemMessage(Component.literal((i + 1) + ". " + trait.name()
                            + " [" + trait.rarity().getDisplayName() + "]" + selectedMark + fixedMark)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(trait.rarity().getColor()))));
            player.sendSystemMessage(Component.literal("   " + trait.description()).withStyle(ChatFormatting.GRAY));
        }
    }
}
