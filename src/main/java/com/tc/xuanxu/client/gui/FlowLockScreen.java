package com.tc.xuanxu.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 觉醒流程锁定屏幕基类。
 * <p>
 * 灵根 / 灵体 / 词条抽取期间禁止按 ESC 退出，必须完成全部觉醒流程。
 * forced 为 false 时（查看模式）不拦截 ESC。
 */
public abstract class FlowLockScreen extends Screen {
    private static final String HINT = "觉醒流程进行中：完成灵根、灵体与词条抽取后方可退出";

    private final boolean forced;

    protected FlowLockScreen(Component title, boolean forced) {
        super(title);
        this.forced = forced;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 强制流程模式下吞掉 ESC，禁止关闭当前流程屏幕
        if (forced && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * 在屏幕最底部绘制流程锁定提示（仅强制模式）。
     * 放在 height-12 是为了避开 height-40 处的底部按钮，避免文字与按钮重叠。
     */
    protected void drawFlowLockHint(GuiGraphics guiGraphics) {
        if (!forced) {
            return;
        }
        guiGraphics.drawString(this.font, HINT,
                (this.width - this.font.width(HINT)) / 2,
                this.height - 12, 0xFFFFFF55);
    }
}