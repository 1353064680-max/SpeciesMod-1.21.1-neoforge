package com.aigoth.species;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.command.SpeciesCommand;
import com.aigoth.species.input.InputEventHandler;
import com.aigoth.species.input.KeyBindings;
import com.aigoth.species.item.ModItems;
import com.aigoth.species.network.PacketHandler;
import com.aigoth.species.event.BaseRaceEventHandler;
import com.aigoth.species.event.RaceEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import org.slf4j.LoggerFactory;

@Mod(SpeciesMod.MOD_ID)
public class SpeciesMod {
    public static final String MOD_ID = "species";
    public static final org.slf4j.Logger log = LoggerFactory.getLogger(SpeciesMod.class);

    public SpeciesMod(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        // 注册物品
        ModItems.register(modEventBus);

        // 注册Capability
        PlayerSpeciesCapability.register(modEventBus);

        // 注册配置
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);

        // 注册模组生命周期事件
        modEventBus.addListener(this::setup);
        
        // 注册网络包
        modEventBus.register(PacketHandler.class);
        
        // 注册按键绑定
        modEventBus.register(KeyBindings.class);

        // 注册游戏事件到 NeoForge 总线
        NeoForge.EVENT_BUS.register(BaseRaceEventHandler.class);
        NeoForge.EVENT_BUS.register(RaceEventHandler.class);
        NeoForge.EVENT_BUS.register(this);
        
        // 注册客户端事件处理器
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.register(InputEventHandler.class);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 初始化完成
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SpeciesCommand.register(event.getDispatcher());
    }
}