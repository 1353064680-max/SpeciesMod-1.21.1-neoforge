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

public class Slimekin extends Race {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public Slimekin() {
        super("slimekin", Component.translatable("race.species.slimekin"),
              Component.translatable("race.species.slimekin.short_desc"),
              Component.translatable("race.species.slimekin.benefits"),
              Component.translatable("race.species.slimekin.drawbacks"),
              Component.translatable("skill.species.slimekin.name"),
              Component.translatable("skill.species.slimekin.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_health_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加击退抗性
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.knockback_resistance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_knockback_resistance_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
        
        // 完全免疫摔落伤害（设置极大的安全摔落距离）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_fall_immunity"), 1024.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加跳跃高度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.jump_strength"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_jump_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：粘液飞溅 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 90; // 90秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 0.0f; // 不消耗饱食度
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 检查并扣除生命值（50%当前生命值，至少保留4点/2颗心）
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float damageToDeal = currentHealth * 0.5f;
        
        // 确保至少保留2颗心（4点生命值）
        if (currentHealth - damageToDeal < 4.0f) {
            damageToDeal = currentHealth - 4.0f;
        }
        
        // 如果无法扣除足够的生命值（血量过低），则不能使用技能
        if (damageToDeal <= 0) {
            player.sendSystemMessage(Component.translatable("skill.species.no_resource_health"));
            LOGGER.debug("玩家 {} 生命值过低，无法使用粘液飞溅技能", player.getName().getString());
            return;
        }
        
        // 扣除生命值（使用魔法伤害，可以穿过护甲）
        player.hurt(player.damageSources().magic(), damageToDeal);
        LOGGER.debug("玩家 {} 使用粘液飞溅，扣除 {} 点生命值（50%当前生命值）", player.getName().getString(), damageToDeal);
        
        // 2. 查找半径8格内所有实体并施加减速效果
        double radius = 8.0;
        int affectedCount = 0;
        
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(radius))) {
            
            // 给予缓慢 IV 效果，持续 15 秒（300 ticks）
            // 等级 3 = 缓慢 IV（0-based）
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3, false, false, false));
            
            // 给予挖掘疲劳 III 效果，持续 15 秒（300 ticks）
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 2, false, false, false));
            
            affectedCount++;
        }
        
        LOGGER.info("粘液飞溅技能影响范围内 {} 个实体，施加缓慢IV和挖掘疲劳III效果，持续15秒", affectedCount);
        
        // 3. 播放音效（史莱姆跳跃声）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.SLIME_JUMP, player.getSoundSource(), 1.0F, 1.0F);
        LOGGER.debug("播放粘液飞溅音效");
        
        // 4. 生成粒子效果（史莱姆粒子）
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (Math.random() - 0.5) * radius * 2;
                double offsetY = Math.random() * 3.0;
                double offsetZ = (Math.random() - 0.5) * radius * 2;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
        LOGGER.debug("生成粘液飞溅粒子效果");
    }
}
