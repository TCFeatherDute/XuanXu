package com.tc.xuanxu.lingen;

import com.tc.xuanxu.network.OpenLingGenScreenPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 灵根相关事件：首次进入世界自动弹出灵根抽取 UI。
 */
public final class LingGenEventHandler {
    private LingGenEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LingGenData data = LingGenManager.get(player);
        if (data != null) {
            return;
        }

        Map<String, Double> emptyRoots = new LinkedHashMap<>();
        for (LingGenType type : LingGenRootRegistry.all()) {
            emptyRoots.put(type.id(), 0.0);
        }

        PacketDistributor.sendToPlayer(player, new OpenLingGenScreenPayload(
                false,
                0.0,
                emptyRoots,
                1.0,
                true
        ));
    }
}
