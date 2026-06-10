package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SpeciesMod.MOD_ID);
        
        // 注册客户端数据包
        registrar.playToClient(OpenRaceSelectionPacket.TYPE, 
            OpenRaceSelectionPacket.STREAM_CODEC,
            OpenRaceSelectionPacket::handle);
            
        // 注册服务端数据包
        registrar.playToServer(RaceSelectionPacket.TYPE,
            RaceSelectionPacket.STREAM_CODEC,
            RaceSelectionPacket::handle);
            
        registrar.playToServer(TogglePotionEffectsPacket.TYPE,
            TogglePotionEffectsPacket.STREAM_CODEC,
            TogglePotionEffectsPacket::handle);
        
        // 注册二段跳请求包
        registrar.playToServer(DoubleJumpRequestPacket.TYPE,
            DoubleJumpRequestPacket.STREAM_CODEC,
            DoubleJumpRequestPacket::handle);
    }
    
    // 添加静态方法用于发送数据包
    public static void sendToServer(CustomPacketPayload packet) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
    }
}
