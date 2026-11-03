package com.tc.xuanxu.client.gui;

import com.tc.xuanxu.lingen.LingGenRank;
import com.tc.xuanxu.lingen.LingGenRootRegistry;
import com.tc.xuanxu.lingen.LingGenType;
import com.tc.xuanxu.network.OpDebugActionPayload;
import com.tc.xuanxu.network.OpenOpDebugPayload;
import com.tc.xuanxu.physique.PhysiqueRegistry;
import com.tc.xuanxu.physique.PhysiqueType;
import com.tc.xuanxu.trait.TraitDefinition;
import com.tc.xuanxu.trait.TraitRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * OP 专用调试面板。
 * <p>
 * 仅管理员可打开（命令 {@code /cultivation op}），用于在创造模式下无限刷新
 * 灵根 / 灵体 / 天命词条，方便测试各品质的展示与数值。
 */
public class OpDebugScreen extends Screen {
    private static final int LABEL_INDENT = 24;

    private OpenOpDebugPayload data;

    public OpDebugScreen(OpenOpDebugPayload data) {
        super(Component.literal("玄墟 · OP 调试面板"));
        this.data = data;
    }

    /**
     * 收到服务端回发的最新状态后就地更新，不重建屏幕（避免闪烁）。
     */
    public void updateState(OpenOpDebugPayload newData) {
        this.data = newData;
    }

    @Override
    protected void init() {
        String[] labels = {"重抽灵根", "重抽灵体", "重抽词条", "重置全部", "关闭"};
        int buttonWidth = 92;
        int buttonHeight = 20;
        int gap = 6;
        int totalWidth = labels.length * buttonWidth + (labels.length - 1) * gap;
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - 30;

        for (int i = 0; i < labels.length; i++) {
            int action = i;
            addRenderableWidget(Button.builder(
                    Component.literal(labels[i]),
                    button -> {
                        if (action == 4) {
                            this.onClose();
                        } else {
                            PacketDistributor.sendToServer(new OpDebugActionPayload(action));
                        }
                    }
            ).bounds(startX + i * (buttonWidth + gap), y, buttonWidth, buttonHeight).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 半透明深色底，避免背景图干扰阅读
        guiGraphics.fill(0, 0, this.width, this.height, 0xD0101010);

        String title = "===== 玄墟 · OP 调试面板 =====";
        guiGraphics.drawString(this.font, title, (this.width - this.font.width(title)) / 2, 14, 0xFFFFD700);
        String hint = "管理员专用 — 可无限刷新灵根 / 灵体 / 词条";
        guiGraphics.drawString(this.font, hint, (this.width - this.font.width(hint)) / 2, 26, 0xFF888888);

        int y = 48;

        // ---------- 灵根 ----------
        guiGraphics.drawString(this.font, "【灵根】", LABEL_INDENT, y, 0xFF55FF55);
        y += 13;
        if (data.roots().isEmpty()) {
            guiGraphics.drawString(this.font, "  尚未觉醒灵根", LABEL_INDENT, y, 0xFF888888);
            y += 12;
        } else {
            guiGraphics.drawString(this.font,
                    "  杂质: " + String.format("%.2f%%", data.impurity())
                            + "    可用: " + String.format("%.2f%%", 100.0 - data.impurity()),
                    LABEL_INDENT, y, 0xFFFFFFFF);
            y += 12;
            for (LingGenType type : LingGenRootRegistry.all()) {
                double percent = data.roots().getOrDefault(type.id(), 0.0);
                LingGenRank rank = LingGenRank.fromPercent(percent);
                guiGraphics.drawString(this.font,
                        "  " + type.displayName() + ": " + String.format("%.2f%%", percent)
                                + "  (" + rank.getDisplayName() + ")",
                        LABEL_INDENT, y, rank.getArgbColor());
                y += 12;
            }
        }
        y += 6;

        // ---------- 灵体 ----------
        guiGraphics.drawString(this.font, "【灵体】", LABEL_INDENT, y, 0xFF55FFFF);
        y += 13;
        String physiqueId = data.physiqueId().orElse(null);
        if (physiqueId == null) {
            guiGraphics.drawString(this.font, "  无灵体（凡体或尚未觉醒）", LABEL_INDENT, y, 0xFF888888);
        } else {
            PhysiqueType physique = PhysiqueRegistry.get(physiqueId).orElse(null);
            if (physique != null) {
                guiGraphics.drawString(this.font,
                        "  " + physique.name() + " [" + physique.rarity().getDisplayName() + "]",
                        LABEL_INDENT, y, 0xFF000000 | physique.rarity().getColor());
            } else {
                guiGraphics.drawString(this.font, "  " + physiqueId + "（未知灵体）", LABEL_INDENT, y, 0xFFFFFFFF);
            }
        }
        y += 18;

        // ---------- 词条 ----------
        guiGraphics.drawString(this.font, "【天命词条】", LABEL_INDENT, y, 0xFFFFAA00);
        y += 13;
        List<String> traitIds = data.traitIds();
        if (traitIds.isEmpty()) {
            guiGraphics.drawString(this.font, "  暂无词条", LABEL_INDENT, y, 0xFF888888);
        } else {
            for (String id : traitIds) {
                TraitDefinition trait = TraitRegistry.get(id).orElse(null);
                if (trait != null) {
                    guiGraphics.drawString(this.font,
                            "  " + trait.name() + " [" + trait.rarity().getDisplayName() + "]",
                            LABEL_INDENT, y, 0xFF000000 | trait.rarity().getColor());
                } else {
                    guiGraphics.drawString(this.font, "  " + id + "（未知词条）", LABEL_INDENT, y, 0xFFFFFFFF);
                }
                y += 12;
            }
        }

        // 底部按钮绘制在自定义文案之上
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}