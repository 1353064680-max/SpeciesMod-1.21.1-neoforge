package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.capability.PlayerSpeciesCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record TogglePotionEffectsPacket() implements CustomPacketPayload {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final Type<TogglePotionEffectsPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "toggle_potion_effects")
    );
    
    public static final StreamCodec<FriendlyByteBuf, TogglePotionEffectsPacket> STREAM_CODEC = 
        StreamCodec.unit(new TogglePotionEffectsPacket());
    
    public TogglePotionEffectsPacket(FriendlyByteBuf buf) {
        this();
    }
    
    public static void handle(TogglePotionEffectsPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                if (ctx.player() instanceof ServerPlayer player) {
                    var cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                    // 切换种族药水效果状态
                    boolean newState = !cap.isRacialEffectsEnabled();
                    cap.setRacialEffectsEnabled(newState);
                    
                    // 向玩家发送消息
                    if (newState) {
                        player.sendSystemMessage(Component.translatable("species.potion_effects_enabled"));
                    } else {
                        player.sendSystemMessage(Component.translatable("species.potion_effects_disabled"));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error handling TogglePotionEffectsPacket", e);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
