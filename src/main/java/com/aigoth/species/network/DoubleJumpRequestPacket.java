package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端发送给服务端的二段跳请求包
 */
public record DoubleJumpRequestPacket() implements CustomPacketPayload {
    
    // 用于跟踪每个玩家是否在本次滞空中已使用过二段跳 - 服务端权威
    // Key: Player UUID, Value: true 表示已使用过二段跳，需要落地后重置
    private static final Map<UUID, Boolean> hasDoubleJumped = new ConcurrentHashMap<>();
    
    public static final Type<DoubleJumpRequestPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "double_jump_request"));
    
    public static final StreamCodec<FriendlyByteBuf, DoubleJumpRequestPacket> STREAM_CODEC = StreamCodec.unit(new DoubleJumpRequestPacket());
    
    public static void handle(final DoubleJumpRequestPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            if (player.level().isClientSide) {
                return;
            }
            
            // 执行二段跳逻辑
            performDoubleJump(player);
        }).exceptionally(e -> {
            SpeciesMod.log.error("处理二段跳请求时出错: {}", e.getMessage());
            return null;
        });
    }
    
    private static void performDoubleJump(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (!cap.hasSelectedRace() || !cap.isRacialEffectsEnabled()) {
            return;
        }
        
        String raceId = cap.getSelectedRace().getRaceId();
        if (!"catkin".equals(raceId) && !"aigoth".equals(raceId) && !"wingedbeast".equals(raceId)) {
            return;
        }
        
        // 检查是否在空中（非地面、非攀爬、非流体）
        if (player.onGround() || player.onClimbable() || player.isInWaterOrBubble()) {
            return;
        }
        
        // 检查玩家是否已经在这次滞空中使用过二段跳
        Boolean alreadyJumped = hasDoubleJumped.get(player.getUUID());
        if (alreadyJumped != null && alreadyJumped) {
            return;
        }
        
        // 检查垂直速度（不能向上太快）
        if (player.getDeltaMovement().y >= 0.1) {
            return;
        }
        
        // === 执行二段跳 ===
        
        // 借鉴 Artifacts 的 DoubleJump.jump 逻辑
        double fallDamageMultiplier = 1.0;
        if (player.fallDistance > 0) {
            player.causeFallDamage(player.fallDistance, (float) fallDamageMultiplier, player.damageSources().fall());
        }
        player.fallDistance = 0;
        
        double jumpHeight = 0.5;
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
            var effect = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP);
            if (effect != null) {
                jumpHeight += 0.1 * (effect.getAmplifier() + 1);
            }
        }
        
        // 关键：直接设置垂直速度为 jumpHeight
        double currentY = player.getDeltaMovement().y;
        
        // 使用 setDeltaMovement 设置新的速度
        player.setDeltaMovement(
            player.getDeltaMovement().x,
            jumpHeight,
            player.getDeltaMovement().z
        );
        
        // 标记需要同步到客户端（重要！）
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(
                serverPlayer.getId(),
                player.getDeltaMovement()
            ));
        }
        
        player.causeFoodExhaustion(0.05F);
        
        // 标记玩家已在本滞空中使用过二段跳
        hasDoubleJumped.put(player.getUUID(), true);
        
        // 播放音效
        player.level().playSound(null, player, net.minecraft.sounds.SoundEvents.RABBIT_JUMP, player.getSoundSource(), 0.5F, 1.0F);
    }
    
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 公开方法：当玩家落地时重置二段跳标志
     * 由 RaceEventHandler.resetDoubleJumpOnLand 调用
     */
    public static void resetOnLand(java.util.UUID playerUUID) {
        hasDoubleJumped.remove(playerUUID);
    }
}
