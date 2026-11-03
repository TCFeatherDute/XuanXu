package com.tc.xuanxu.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tc.xuanxu.network.OpenTraitSelectPayload;
import com.tc.xuanxu.network.TraitChoosePayload;
import com.tc.xuanxu.network.TraitConfirmPayload;
import com.tc.xuanxu.network.TraitFixPayload;
import com.tc.xuanxu.network.TraitRerollPayload;
import com.tc.xuanxu.trait.TraitDefinition;
import com.tc.xuanxu.trait.TraitRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 天命词条十选四 GUI。
 */
public class TraitSelectScreen extends FlowLockScreen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/lingen_background.png");
    private static final int GL_TEXTURE_2D = 3553;
    private static final int GL_TEXTURE_MIN_FILTER = 10241;
    private static final int GL_TEXTURE_MAG_FILTER = 10240;
    private static final int GL_NEAREST = 9728;

    private final OpenTraitSelectPayload data;
    private final Map<Button, TraitDefinition> traitByButton = new HashMap<>();

    public TraitSelectScreen(OpenTraitSelectPayload data, boolean forced) {
        super(Component.literal("天命词条"), forced);
        this.data = data;
    }

    @Override
    protected void init() {
        // 背景纹理最近邻过滤只需设置一次，避免每帧重复设置 GL 状态
        RenderSystem.setShaderTexture(0, BACKGROUND);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int cardWidth = 150;
        int cardHeight = 20;
        int gapX = 10;
        int gapY = 6;
        // 列数按实际屏幕宽度自适应：避免在 GUI 缩放较大（逻辑宽度较小）时网格溢出屏幕
        int maxColumns = 5;
        int columns = Math.max(1, Math.min(maxColumns, (this.width - 20 + gapX) / (cardWidth + gapX)));
        int gridWidth = columns * cardWidth + (columns - 1) * gapX;
        int startX = (this.width - gridWidth) / 2;
        int startY = 48;

        List<String> options = data.options();
        for (int i = 0; i < options.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = startX + col * (cardWidth + gapX);
            int y = startY + row * (cardHeight + gapY);
            int index = i;
            String id = options.get(i);
            TraitDefinition trait = TraitRegistry.get(id).orElse(null);

            // 按稀有度给文字着色：白/绿/蓝/紫/橙
            MutableComponent label;
            if (trait != null) {
                label = Component.literal((i + 1) + ". " + trait.name() + " [" + trait.rarity().getDisplayName() + "]")
                        .withStyle(style -> style.withColor(trait.rarity().getColor()));
            } else {
                label = Component.literal((i + 1) + ". 未知").withStyle(ChatFormatting.GRAY);
            }
            if (data.selected().contains(id)) {
                label.append(Component.literal(" ✓").withStyle(ChatFormatting.GREEN));
            }
            if (data.fixedId().map(id::equals).orElse(false)) {
                label.append(Component.literal(" 固定").withStyle(ChatFormatting.YELLOW));
            }

            Button button = addRenderableWidget(Button.builder(
                    label,
                    btn -> PacketDistributor.sendToServer(new TraitChoosePayload(index))
            ).bounds(x, y, cardWidth, cardHeight).build());
            if (trait != null) {
                traitByButton.put(button, trait);
            }
        }

        int bottomY = this.height - 40;
        int buttonWidth = 100;
        int totalWidth = buttonWidth * 3 + 20;
        int startBottomX = (this.width - totalWidth) / 2;

        addRenderableWidget(Button.builder(
                Component.literal("刷新(" + data.rerollsLeft() + ")"),
                button -> PacketDistributor.sendToServer(new TraitRerollPayload())
        ).bounds(startBottomX, bottomY, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("固定"),
                button -> PacketDistributor.sendToServer(new TraitFixPayload())
        ).bounds(startBottomX + buttonWidth + 10, bottomY, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("确认(" + data.selected().size() + "/4)"),
                button -> PacketDistributor.sendToServer(new TraitConfirmPayload())
        ).bounds(startBottomX + (buttonWidth + 10) * 2, bottomY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 不调用 renderBackground()，避免原版暗色遮罩，也避免按钮被重复渲染。
        var window = this.minecraft.getWindow();
        float guiScale = (float) window.getGuiScale();
        int screenWidth = window.getScreenWidth();
        int screenHeight = window.getScreenHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.0f / guiScale, 1.0f / guiScale, 1.0f);
        guiGraphics.blit(BACKGROUND, 0, 0, screenWidth, screenHeight,
                0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(this.font, "===== 天命词条 · 十选四 =====",
                this.width / 2 - 60, 22, 0xFFFFD700);
        guiGraphics.drawString(this.font,
                "已选 " + data.selected().size() + "/4    剩余刷新 " + data.rerollsLeft() + " 次"
                        + (data.fixedId().map(id -> "    已固定：" + id).orElse("")),
                this.width / 2 - 100, 34, 0xFFFFFFFF);

        // 在自定义 UI 最上层重新绘制按钮
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        drawFlowLockHint(guiGraphics);

        // 检测鼠标悬停在词条卡片上，显示 tooltip
        for (Map.Entry<Button, TraitDefinition> entry : traitByButton.entrySet()) {
            Button button = entry.getKey();
            if (button.isHoveredOrFocused()) {
                List<Component> tooltip = buildTraitTooltip(entry.getValue(), data);
                if (!tooltip.isEmpty()) {
                    guiGraphics.renderTooltip(this.font,
                            tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                break;
            }
        }
    }

    /**
     * 构建词条 tooltip，展示新字段：稀有度、描述、效果、需求、标签、废弃信息。
     */
    private List<Component> buildTraitTooltip(TraitDefinition trait, OpenTraitSelectPayload data) {
        List<Component> lines = new ArrayList<>();
        if (trait == null) {
            return lines;
        }

        int color = trait.rarity().getColor();
        MutableComponent title = Component.literal(trait.name() + " [" + trait.rarity().getDisplayName() + "]")
                .withStyle(style -> style.withColor(color));
        lines.add(title);

        if (trait.description() != null && !trait.description().isBlank()) {
            lines.add(Component.literal(trait.description()).withStyle(ChatFormatting.GRAY));
        }

        // 占位：属性计算功能暂未开放
        lines.add(Component.literal("效果功能未实装").withStyle(ChatFormatting.DARK_GRAY));

        // 已选/固定状态
        if (data.selected().contains(trait.id())) {
            lines.add(Component.literal("✓ 已选择").withStyle(ChatFormatting.GREEN));
        }
        if (data.fixedId().map(trait.id()::equals).orElse(false)) {
            lines.add(Component.literal("◎ 已固定").withStyle(ChatFormatting.YELLOW));
        }

        // 废弃/迁移
        if (trait.deprecated()) {
            lines.add(Component.literal("⚠ 已废弃" + (trait.replaceBy().isBlank() ? "" : " → " + trait.replaceBy()))
                    .withStyle(ChatFormatting.DARK_RED));
        }

        return lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
