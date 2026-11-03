package com.tc.xuanxu.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tc.xuanxu.lingen.LingGenRank;
import com.tc.xuanxu.lingen.LingGenRootRegistry;
import com.tc.xuanxu.lingen.LingGenType;
import com.tc.xuanxu.network.OpenLingGenScreenPayload;
import com.tc.xuanxu.network.RequestLingGenDrawPayload;
import com.tc.xuanxu.network.RequestPhysiqueSelectPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 灵根面板 UI。
 */
public class LingGenScreen extends FlowLockScreen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/lingen_background.png");
    private static final int GL_TEXTURE_2D = 3553;
    private static final int GL_TEXTURE_MIN_FILTER = 10241;
    private static final int GL_TEXTURE_MAG_FILTER = 10240;
    private static final int GL_NEAREST = 9728;
    private static final Map<String, ResourceLocation> ELEMENT_ICONS = Map.of(
            "gold", ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/element_gold.png"),
            "wood", ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/element_wood.png"),
            "water", ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/element_water.png"),
            "fire", ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/element_fire.png"),
            "earth", ResourceLocation.fromNamespaceAndPath("xuanxu", "textures/gui/element_earth.png")
    );

    private final OpenLingGenScreenPayload data;
    private final List<LingGenRow> rows = new ArrayList<>();
    private int guiLeft;
    private int guiTop;
    private int panelWidth;
    private int panelHeight;

    private record LingGenRow(LingGenType type, double percent, LingGenRank rank, int y) {
    }

    public LingGenScreen(OpenLingGenScreenPayload data) {
        super(Component.literal("灵根面板"), data.forced());
        this.data = data;
    }

    @Override
    protected void init() {
        // 面板尺寸跟随窗口，文字绘制基于全屏布局
        this.panelWidth = this.width;
        this.panelHeight = this.height;
        this.guiLeft = 0;
        this.guiTop = 0;

        // 将背景纹理的过滤方式改为最近邻采样，避免 Minecraft 默认线性/柔化过滤导致的整体模糊。
        // 只需设置一次，避免每帧重复设置 GL 状态。
        RenderSystem.setShaderTexture(0, BACKGROUND);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int buttonWidth = 160;
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = this.height - 40;

        if (data.hasData()) {
            addRenderableWidget(Button.builder(
                    Component.literal("下一步：抽取灵体"),
                    button -> PacketDistributor.sendToServer(new RequestPhysiqueSelectPayload())
            ).bounds(buttonX, buttonY, buttonWidth, 20).build());
        } else {
            addRenderableWidget(Button.builder(
                    Component.literal("抽取灵根"),
                    button -> PacketDistributor.sendToServer(new RequestLingGenDrawPayload())
            ).bounds(buttonX, buttonY, buttonWidth, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 不调用 super.render()/renderBackground()，避免原版 GUI 的暗色遮罩/滤镜效果，
        // 也避免按钮在这里被重复渲染。按钮在自定义背景/文案之上统一绘制一次。

        // 全屏覆盖：背景图铺满整个游戏窗口
        var window = this.minecraft.getWindow();
        float guiScale = (float) window.getGuiScale();
        int screenWidth = window.getScreenWidth();
        int screenHeight = window.getScreenHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.0f / guiScale, 1.0f / guiScale, 1.0f);
        guiGraphics.blit(BACKGROUND, 0, 0, screenWidth, screenHeight,
                0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
        guiGraphics.pose().popPose();

        // 已抽取灵根时，在背景之上显示灵根数据
        if (data.hasData()) {
            drawTitle(guiGraphics);
            drawImpurity(guiGraphics);
            drawRoots(guiGraphics);
            drawMultiplier(guiGraphics);
        }

        // 在自定义 UI 最上层重新绘制按钮，避免被背景图盖住
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        drawFlowLockHint(guiGraphics);

        // 灵根行悬停 tooltip
        if (data.hasData()) {
            for (LingGenRow row : rows) {
                int rowLeft = guiLeft + 22;
                int rowRight = guiLeft + panelWidth - 22;
                if (mouseX >= rowLeft && mouseX <= rowRight && mouseY >= row.y && mouseY <= row.y + 18) {
                    guiGraphics.renderTooltip(this.font,
                            buildLingGenTooltip(row).stream().map(Component::getVisualOrderText).collect(Collectors.toList()),
                            mouseX, mouseY);
                    break;
                }
            }
        }
    }

    private List<Component> buildLingGenTooltip(LingGenRow row) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(row.type().displayName() + " (" + row.rank().getDisplayName() + ")")
                .withStyle(style -> style.withColor(row.rank().getColor())));
        lines.add(Component.literal("该系灵根占比: " + String.format("%.2f%%", row.percent()))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("属性计算功能暂未开放").withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private void drawTitle(GuiGraphics guiGraphics) {
        String title = "===== 玄墟 · 灵根面板 =====";
        int x = guiLeft + (panelWidth - this.font.width(title)) / 2;
        guiGraphics.drawString(this.font, title, x, guiTop + 14, 0xFFFFD700);
    }

    private void drawImpurity(GuiGraphics guiGraphics) {
        String text = "杂质: " + String.format("%.2f%%", data.impurity())
                + "    可用空间: " + String.format("%.2f%%", 100.0 - data.impurity());
        int x = guiLeft + (panelWidth - this.font.width(text)) / 2;
        guiGraphics.drawString(this.font, text, x, guiTop + 34, 0xFFFFFFFF);
    }

    private void drawRoots(GuiGraphics guiGraphics) {
        rows.clear();
        int y = guiTop + 52;
        for (LingGenType type : LingGenRootRegistry.all()) {
            double percent = data.roots().getOrDefault(type.id(), 0.0);
            LingGenRank rank = LingGenRank.fromPercent(percent);
            ResourceLocation icon = ELEMENT_ICONS.get(type.id());
            if (icon != null) {
                guiGraphics.blit(icon, guiLeft + 22, y, 16, 16, 0, 0, 16, 16, 16, 16);
            }
            guiGraphics.drawString(this.font,
                    type.displayName() + ": " + String.format("%.2f%%", percent) + " (" + rank.getDisplayName() + ")",
                    guiLeft + 46, y + 4, rank.getArgbColor());
            rows.add(new LingGenRow(type, percent, rank, y));
            y += 20;
        }
    }

    private void drawMultiplier(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font,
                "修炼速度倍率: " + String.format("%.2f", data.cultivationMultiplier()),
                guiLeft + 30, guiTop + panelHeight - 45, 0xFF55FF55);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}