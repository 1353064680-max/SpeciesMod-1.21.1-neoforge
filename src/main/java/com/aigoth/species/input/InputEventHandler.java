package com.aigoth.species.input;

import com.aigoth.species.network.PacketHandler;
import com.aigoth.species.network.TogglePotionEffectsPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class InputEventHandler {
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        
        // 检查是否按下了切换种族药水效果的按键
        if (mc.player != null && KeyBindings.TOGGLE_POTION_EFFECTS.consumeClick()) {
            // 发送数据包到服务端处理
            PacketHandler.sendToServer(new TogglePotionEffectsPacket());
        }
    }
}