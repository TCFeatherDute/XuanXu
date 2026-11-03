package com.tc.xuanxu.client.gui;

import com.tc.xuanxu.lingen.LingGenRank;
import com.tc.xuanxu.lingen.LingGenRootRegistry;
import com.tc.xuanxu.lingen.LingGenType;
import com.tc.xuanxu.network.OpenStatusPayload;
import com.tc.xuanxu.physique.PhysiqueRegistry;
import com.tc.xuanxu.physique.PhysiqueType;
import com.tc.xuanxu.trait.TraitDefinition;
import com.tc.xuanxu.trait.TraitRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * B 键打开的角色状态面板，展示灵根、灵体、词条信息。
 * 属性计算功能暂未开放，仅显示基础数据。
 */
public class StatusScreen extends Screen {
    private final OpenStatusPayload data;

    public StatusScreen(OpenStatusPayload data) {
        super(Component.literal("玄墟 · 角色状态"));
        this.data = data;
    }

    @Override
    protected void init() {
        int buttonWidth = 120;
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = this.height - 40;

        addRenderableWidget(Button.builder(
                Component.literal("关闭"),
                button -> this.onClose()
        ).bounds(buttonX, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int y = 20;

        // 标题
        guiGraphics.drawString(this.font, "===== 玄墟 · 角色状态 =====",
                (this.width - this.font.width("===== 玄墟 · 角色状态 =====")) / 2,
                y, 0xFFFFD700);
        y += 20;

        // 灵根部分
        guiGraphics.drawString(this.font, "【灵根】", 10, y, 0xFF55FF55);
        y += 12;
        if (data.hasLingGen()) {
            guiGraphics.drawString(this.font, " 杂质: " + String.format("%.2f%%", data.impurity()), 10, y, 0xFFFFFFFF);
            y += 10;
            guiGraphics.drawString(this.font, " 可用空间: " + String.format("%.2f%%", 100.0 - data.impurity()), 10, y, 0xFFFFFFFF);
            y += 10;
            for (LingGenType type : LingGenRootRegistry.all()) {
                double percent = data.roots().getOrDefault(type.id(), 0.0);
                LingGenRank rank = LingGenRank.fromPercent(percent);
                String line = "  " + type.displayName() + ": " + String.format("%.2f%%", percent) + " (" + rank.getDisplayName() + ")";
                guiGraphics.drawString(this.font, line, 10, y, rank.getArgbColor());
                y += 10;
            }
        } else {
            guiGraphics.drawString(this.font, "  (尚未觉醒灵根)", 10, y, 0xFF888888);
            y += 10;
        }
        y += 5;

        // 灵体部分
        guiGraphics.drawString(this.font, "【灵体】", 10, y, 0xFF55FFFF);
        y += 12;
        if (data.physiqueResolved()) {
            if (data.physiqueId() != null) {
                PhysiqueType physique = PhysiqueRegistry.get(data.physiqueId()).orElse(null);
                if (physique != null) {
                    guiGraphics.drawString(this.font, "  " + physique.name() + " [" + physique.rarity().getDisplayName() + "]",
                            10, y, 0xFF000000 | physique.rarity().getColor());
                    y += 10;
                    guiGraphics.drawString(this.font, "  " + physique.description(), 10, y, 0xFF888888);
                    y += 10;
                } else {
                    guiGraphics.drawString(this.font, "  " + data.physiqueId() + " (未知灵体)", 10, y, 0xFFFFFFFF);
                    y += 10;
                }
            } else {
                guiGraphics.drawString(this.font, "  凡体", 10, y, 0xFF888888);
                y += 10;
            }
        } else {
            guiGraphics.drawString(this.font, "  (尚未觉醒灵体)", 10, y, 0xFF888888);
            y += 10;
        }
        y += 5;

        // 词条部分
        guiGraphics.drawString(this.font, "【天命词条】", 10, y, 0xFFFFAA00);
        y += 12;
        List<String> traitIds = data.traitIds();
        if (traitIds.isEmpty()) {
            guiGraphics.drawString(this.font, "  (暂无词条)", 10, y, 0xFF888888);
            y += 10;
        } else {
            for (String id : traitIds) {
                TraitDefinition trait = TraitRegistry.get(id).orElse(null);
                if (trait != null) {
                    guiGraphics.drawString(this.font, "  " + trait.name() + " [" + trait.rarity().getDisplayName() + "]",
                            10, y, 0xFF000000 | trait.rarity().getColor());
                    y += 10;
                    guiGraphics.drawString(this.font, "  " + trait.description(), 10, y, 0xFF888888);
                    y += 10;
                } else {
                    guiGraphics.drawString(this.font, "  " + id + " (未知词条)", 10, y, 0xFFFFFFFF);
                    y += 10;
                }
            }
        }

        // 占位提示
        y += 10;
        guiGraphics.drawString(this.font, "— 属性计算功能暂未开放 —",
                (this.width - this.font.width("— 属性计算功能暂未开放 —")) / 2,
                y, 0xFF888888);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}