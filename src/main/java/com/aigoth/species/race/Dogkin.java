package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dogkin extends Race {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public Dogkin() {
        super("dogkin", Component.translatable("race.species.dogkin"),
              Component.translatable("race.species.dogkin.short_desc"),
              Component.translatable("race.species.dogkin.benefits"),
              Component.translatable("race.species.dogkin.drawbacks"),
              Component.translatable("skill.species.dogkin.name"),
              Component.translatable("skill.species.dogkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 增加攻击伤害
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "dogkin_attack_damage_bonus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：嗜血狂暴 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 45; // 45秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 0.0f; // 不消耗饱食度
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 检查并扣除生命值（8.0点 = 4颗心）
        float currentHealth = player.getHealth();
        
        // 如果生命值不足8点，不能使用技能
        if (currentHealth < 8.0f) {
            player.sendSystemMessage(Component.translatable("skill.species.no_resource_health"));
            LOGGER.debug("玩家 {} 生命值不足，无法使用嗜血狂暴技能", player.getName().getString());
            return;
        }
        
        // 扣除8.0点生命值（使用魔法伤害）
        player.hurt(player.damageSources().magic(), 8.0f);
        LOGGER.debug("玩家 {} 使用嗜血狂暴，扣除 8.0 生命值（4颗心）", player.getName().getString());
        
        // 2. 给予自身力量 II 效果，持续 15 秒（300 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, false, false, false));
        LOGGER.debug("玩家 {} 获得力量II效果，持续 15 秒", player.getName().getString());
        
        // 3. 给予自身急迫 II 效果，持续 15 秒（300 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 1, false, false, false));
        LOGGER.debug("玩家 {} 获得急迫II效果，持续 15 秒", player.getName().getString());
        
        // 4. 查找半径10格内所有实体并施加发光效果
        double radius = 10.0;
        int affectedCount = 0;
        
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(radius))) {
            
            // 排除自己
            if (entity == player) continue;
            
            // 给予发光效果，持续 15 秒（300 ticks）
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false, true));
            
            affectedCount++;
        }
        
        LOGGER.info("嗜血狂暴技能影响范围内 {} 个实体，施加发光效果，持续15秒", affectedCount);
        
        // 5. 播放音效（狼嚎声）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.WOLF_HOWL, player.getSoundSource(), 1.0F, 1.0F);
        LOGGER.debug("播放嗜血狂暴音效");
        
        // 6. 生成粒子效果（愤怒粒子）
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
        LOGGER.debug("生成嗜血狂暴粒子效果");
    }
}
