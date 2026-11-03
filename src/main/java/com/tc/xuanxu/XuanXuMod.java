package com.tc.xuanxu;

import com.mojang.logging.LogUtils;
import com.tc.xuanxu.client.ClientKeyHandler;
import com.tc.xuanxu.command.CultivationCommands;
import com.tc.xuanxu.lingen.LingGenConfig;
import com.tc.xuanxu.lingen.LingGenEventHandler;
import com.tc.xuanxu.lingen.LingGenRootRegistry;
import com.tc.xuanxu.network.ModPayloads;
import com.tc.xuanxu.physique.PhysiqueEventHandler;
import com.tc.xuanxu.physique.PhysiqueRegistry;
import com.tc.xuanxu.trait.TraitPoolRegistry;
import com.tc.xuanxu.trait.TraitRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(XuanXuMod.MOD_ID)
public final class XuanXuMod {
    public static final String MOD_ID = "xuanxu";
    public static final String MOD_NAME = "XuanXu Mod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public XuanXuMod(IEventBus modEventBus) {
        LingGenConfig.init();
        LingGenRootRegistry.init();
        PhysiqueRegistry.init();
        TraitRegistry.init();
        TraitPoolRegistry.init();
        NeoForge.EVENT_BUS.register(LingGenEventHandler.class);
        NeoForge.EVENT_BUS.register(PhysiqueEventHandler.class);
        NeoForge.EVENT_BUS.register(CultivationCommands.class);
        modEventBus.addListener(ModPayloads::register);
        modEventBus.addListener(this::commonSetup);

        // 注册客户端按键绑定与按键事件（仅在客户端生效）
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(ClientKeyHandler::registerKeyMappings);
            NeoForge.EVENT_BUS.register(ClientKeyHandler.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("{} 初始化完成！", MOD_NAME);
    }
}
