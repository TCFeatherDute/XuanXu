package com.tc.xuanxu.client;

import com.tc.xuanxu.client.gui.LingGenScreen;
import com.tc.xuanxu.client.gui.OpDebugScreen;
import com.tc.xuanxu.client.gui.PhysiqueSelectScreen;
import com.tc.xuanxu.client.gui.StatusScreen;
import com.tc.xuanxu.client.gui.TraitSelectScreen;
import com.tc.xuanxu.network.CloseTraitScreenPayload;
import com.tc.xuanxu.network.OpenLingGenScreenPayload;
import com.tc.xuanxu.network.OpenOpDebugPayload;
import com.tc.xuanxu.network.OpenPhysiqueSelectPayload;
import com.tc.xuanxu.network.OpenStatusPayload;
import com.tc.xuanxu.network.OpenTraitSelectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * 客户端网络包处理。
 */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void openLingGenScreen(OpenLingGenScreenPayload payload) {
        Minecraft.getInstance().setScreen(new LingGenScreen(payload));
    }

    public static void openPhysiqueSelect(OpenPhysiqueSelectPayload payload) {
        Minecraft.getInstance().setScreen(new PhysiqueSelectScreen(payload, true));
    }

    public static void openTraitSelect(OpenTraitSelectPayload payload) {
        Minecraft.getInstance().setScreen(new TraitSelectScreen(payload, true));
    }

    public static void closeTraitScreen(CloseTraitScreenPayload payload) {
        Minecraft.getInstance().setScreen(null);
    }

    public static void openStatusScreen(OpenStatusPayload payload) {
        Minecraft.getInstance().setScreen(new StatusScreen(payload));
    }

    /**
     * 打开 OP 调试面板。
     * 若面板已经打开，则就地更新状态而不是重建屏幕，避免每次点击都闪烁。
     */
    public static void openOpDebugScreen(OpenOpDebugPayload payload) {
        Screen current = Minecraft.getInstance().screen;
        if (current instanceof OpDebugScreen debugScreen) {
            debugScreen.updateState(payload);
        } else {
            Minecraft.getInstance().setScreen(new OpDebugScreen(payload));
        }
    }
}