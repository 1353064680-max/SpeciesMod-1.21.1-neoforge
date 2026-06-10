package com.aigoth.species.input;

import com.aigoth.species.network.DoubleJumpRequestPacket;
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
        
        // 检测二段跳输入
        if (mc.player != null) {
            handleDoubleJumpInput(mc.player);
        }
    }
    
    private static void handleDoubleJumpInput(net.minecraft.world.entity.player.Player player) {
        // 只在客户端检测按键并发送请求，不在客户端检查种族（因为 AttachmentType 不同步）
        if (!player.level().isClientSide) {
            return;
        }
        
        // 检查是否在空中
        if (player.onGround() || player.onClimbable() || player.isInWaterOrBubble()) {
            return;
        }
        
        // 检查垂直速度（不能向上太快）
        if (player.getDeltaMovement().y >= 0.1) {
            return;
        }
        
        // 通过 Minecraft 实例的输入管理器检测跳跃键
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyJump.isDown()) {
            // 发送二段跳请求到服务端（种族验证和冷却控制都在服务端进行）
            PacketHandler.sendToServer(new DoubleJumpRequestPacket());
        }
    }
}