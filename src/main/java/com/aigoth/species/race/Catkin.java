package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class Catkin extends Race {
    
    public Catkin() {
        super("catkin", Component.translatable("race.species.catkin"),
              Component.translatable("race.species.catkin.short_desc"),
              Component.translatable("race.species.catkin.benefits"),
              Component.translatable("race.species.catkin.drawbacks"),
              Component.translatable("skill.species.catkin.name"),
              Component.translatable("skill.species.catkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_health_penalty"), -2.0, Operation.ADD_VALUE));
        
        // 减少攻击伤害
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_attack_damage_penalty"), -4.0, Operation.ADD_VALUE));

        // 增加速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_speed_bonus"), 0.2, Operation.ADD_MULTIPLIED_TOTAL));
        
        // 增加摔落缓冲距离（8格以下免疫）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_fall_resistance_bonus"), 8.0, Operation.ADD_VALUE));

        // 减少攻击速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_attack_speed_penalty"), -0.5, Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：哈气 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 30; // 30秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 24.0f; // 24.0 疲劳值 = 6 点饱食度（3个鸡腿）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除饱食度（24.0 疲劳值 = 6 点饱食度 = 3个鸡腿）
        player.getFoodData().addExhaustion(24.0f);
        
        // 2. 给予玩家自身增益效果（力量I + 速度I，持续10秒）
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, false, false)); // 力量I
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false, false)); // 速度I
        
        // 3. 播放猫哈气音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.CAT_HISS, player.getSoundSource(), 1.0F, 1.0F);
        
        // 4. 生成愤怒村民粒子效果（在服务端需要使用 ServerLevel）
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = Math.random() * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                    player.getX() + offsetX,
                    player.getY() + 1.5 + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
        
        // 5. 查找半径3格内所有生物并施加减益效果
        double radius = 3.0;
        
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(radius))) {
            // 排除自己
            if (entity == player) continue;
            
            // 给予虚弱I和缓慢I，持续10秒
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, false, false));
        }
    }
}