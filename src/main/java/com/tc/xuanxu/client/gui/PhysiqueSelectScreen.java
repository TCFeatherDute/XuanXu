package com.tc.xuanxu.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tc.xuanxu.network.ChoosePhysiquePayload;
import com.tc.xuanxu.network.OpenPhysiqueSelectPayload;
import com.tc.xuanxu.physique.PhysiqueRegistry;
import com.tc.xuanxu.physique.PhysiqueType;
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
 * 灵体三选一界面。
 */
public class PhysiqueSelectScreen extends FlowLockScreen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/lingen_background.png");
    private static final int GL_TEXTURE_2D = 3553;
    private static final int GL_TEXTURE_MIN_FILTER = 10241;
    private static final int GL_TEXTURE_MAG_FILTER = 10240;
    private static final int GL_NEAREST = 9728;

    private final List<String> optionIds;
    private final Map<Button, PhysiqueType> physiqueByButton = new HashMap<>();

    public PhysiqueSelectScreen(OpenPhysiqueSelectPayload payload, boolean forced) {
        super(Component.literal("灵体觉醒"), forced);
        this.optionIds = payload.optionIds();
    }

    @Override
    protected void init() {
        // 背景纹理最近邻过滤只需设置一次，避免每帧重复设置 GL 状态
        RenderSystem.setShaderTexture(0, BACKGROUND);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int buttonWidth = 220;
        int buttonHeight = 40;
        int startY = this.height / 2 - 60;

        for (int i = 0; i < optionIds.size(); i++) {
            int index = i;
            String id = optionIds.get(i);
            PhysiqueType type = PhysiqueRegistry.get(id).orElse(null);
            // 按品质给选项文字着色：绿色/蓝色/紫色/橙色/红色
            MutableComponent label;
            if (type != null) {
                label = Component.literal((i + 1) + ". " + type.name() + " [" + type.rarity().getDisplayName() + "]")
                        .withStyle(style -> style.withColor(type.rarity().getColor()));
            } else {
                label = Component.literal((i + 1) + ". 未知灵体").withStyle(ChatFormatting.GRAY);
            }

            Button button = addRenderableWidget(Button.builder(
                    label,
                    btn -> {
                        PacketDistributor.sendToServer(new ChoosePhysiquePayload(index));
                        this.onClose();
                    }
            ).bounds(
                    this.width / 2 - buttonWidth / 2,
                    startY + i * (buttonHeight + 10),
                    buttonWidth,
                    buttonHeight
            ).build());
            if (type != null) {
                physiqueByButton.put(button, type);
            }
        }
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

        guiGraphics.drawString(this.font, "===== 灵体觉醒 · 三选一 =====",
                this.width / 2 - 70, this.height / 2 - 100, 0xFFFF55FF);

        // 在自定义 UI 最上层重新绘制按钮，避免被背景图盖住
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        drawFlowLockHint(guiGraphics);

        // 检测鼠标悬停在灵体卡片上，显示 tooltip
        for (Map.Entry<Button, PhysiqueType> entry : physiqueByButton.entrySet()) {
            Button button = entry.getKey();
            if (button.isHoveredOrFocused()) {
                List<Component> tooltip = buildPhysiqueTooltip(entry.getValue());
                if (!tooltip.isEmpty()) {
                    guiGraphics.renderTooltip(this.font,
                            tooltip.stream().map(Component::getVisualOrderText).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                break;
            }
        }
    }

    private List<Component> buildPhysiqueTooltip(PhysiqueType type) {
        List<Component> lines = new ArrayList<>();
        if (type == null) {
            return lines;
        }
        MutableComponent title = Component.literal(type.name() + " [" + type.rarity().getDisplayName() + "]")
                .withStyle(style -> style.withColor(type.rarity().getColor()));
        lines.add(title);
        if (type.description() != null && !type.description().isBlank()) {
            lines.add(Component.literal(type.description()).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
