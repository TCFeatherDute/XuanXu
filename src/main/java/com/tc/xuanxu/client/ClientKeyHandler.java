package com.tc.xuanxu.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tc.xuanxu.network.RequestStatusPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键绑定与事件处理。
 */
public final class ClientKeyHandler {
    private ClientKeyHandler() {
    }

    public static KeyMapping openStatusKey;

    /**
     * 注册按键（在 MOD 事件总线上调用）。
     */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openStatusKey = new KeyMapping(
                "key.xuanxu.open_status",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.xuanxu"
        );
        event.register(openStatusKey);
    }

    /**
     * 检测按键按下（在 GAME 事件总线上调用）。
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (openStatusKey == null) {
            return;
        }
        // 确保是按下动作，且未打开任何 GUI 时触发
        if (event.getAction() == GLFW.GLFW_PRESS
                && event.getKey() == openStatusKey.getKey().getValue()
                && Minecraft.getInstance().screen == null) {
            PacketDistributor.sendToServer(new RequestStatusPayload());
        }
    }
}