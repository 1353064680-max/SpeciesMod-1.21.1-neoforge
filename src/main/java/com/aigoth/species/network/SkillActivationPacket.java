package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.race.Race;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * 客户端发送给服务端的技能激活请求包
 */
public record SkillActivationPacket() implements CustomPacketPayload {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final Type<SkillActivationPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "skill_activation")
    );
    
    public static final StreamCodec<FriendlyByteBuf, SkillActivationPacket> STREAM_CODEC = 
        StreamCodec.unit(new SkillActivationPacket());
    
    public static void handle(SkillActivationPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                
                // 检查玩家是否已选择种族
                if (!cap.hasSelectedRace()) {
                    LOGGER.debug("玩家 {} 尝试使用技能但未选择种族", player.getName().getString());
                    return;
                }
                
                Race race = cap.getSelectedRace();
                
                // 检查该种族是否有主动技能（人族没有技能）
                if (!race.hasActiveSkill()) {
                    LOGGER.debug("玩家 {} 的种族 {} 没有主动技能", player.getName().getString(), race.getRaceId());
                    return;
                }
                
                // 检查种族效果是否开启
                if (!cap.isRacialEffectsEnabled()) {
                    LOGGER.debug("玩家 {} 的种族效果已关闭，无法使用技能", player.getName().getString());
                    return;
                }
                
                // 检查冷却时间
                if (!cap.canUseSkill(race.getRaceId())) {
                    long remainingSeconds = cap.getSkillCooldownRemaining(race.getRaceId());
                    player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("skill.species.on_cooldown", remainingSeconds)
                    );
                    LOGGER.debug("玩家 {} 的技能处于冷却中，剩余 {} 秒", player.getName().getString(), remainingSeconds);
                    return;
                }
                
                // 检查资源是否足够
                float healthCost = race.getHealthCost();
                float foodCost = race.getFoodExhaustion();
                
                if (healthCost > 0 && player.getHealth() <= healthCost) {
                    player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("skill.species.no_resource_health")
                    );
                    LOGGER.debug("玩家 {} 生命值不足，无法使用技能", player.getName().getString());
                    return;
                }
                
                // 执行技能
                LOGGER.info("玩家 {} 激活了 {} 种族的技能", player.getName().getString(), race.getRaceId());
                race.executeSkill(player);
                
                // 设置冷却时间（单位：秒）
                int cooldownSeconds = race.getSkillCooldownTicks();
                cap.setSkillCooldown(race.getRaceId(), cooldownSeconds);
                
                // 发送成功消息
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("skill.species.activated", race.getSkillName())
                );
                
                LOGGER.debug("技能冷却设置为 {} 秒", cooldownSeconds);
            }
        }).exceptionally(e -> {
            LOGGER.error("处理技能激活包时出错: {}", e.getMessage());
            return null;
        });
    }
    
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
