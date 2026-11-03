package com.tc.xuanxu.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * OP 调试面板的访问控制。
 * <p>
 * 允许以下任意一种情况使用：
 * <ul>
 *   <li>拥有管理员权限（权限等级 2，例如多人服务器的 OP、单人世界开启作弊）；</li>
 *   <li>单人世界的创造模式玩家（方便在创造模式测试时无需额外开作弊）。</li>
 * </ul>
 */
public final class OpPanelAccess {
    private OpPanelAccess() {
    }

    public static boolean allowed(ServerPlayer player) {
        if (player.hasPermissions(2)) {
            return true;
        }
        MinecraftServer server = player.getServer();
        return server != null && server.isSingleplayer() && player.isCreative();
    }

    public static boolean allowed(CommandSourceStack source) {
        if (source.hasPermission(2)) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        return player != null && allowed(player);
    }
}