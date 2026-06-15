package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class Aquatic extends Race {
    
    public Aquatic() {
        super("aquatic", Component.translatable("race.species.aquatic"), 
              Component.translatable("race.species.aquatic.short_desc"),
              Component.translatable("race.species.aquatic.benefits"),
              Component.translatable("race.species.aquatic.drawbacks"),
              Component.translatable("skill.species.aquatic.name"),
              Component.translatable("skill.species.aquatic.description"));
    }

    @Override
    protected void initAttributes() {
        // 增加游泳速度
        attributeModifiers.put(ResourceLocation.fromNamespaceAndPath("neoforge", "swim_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aquatic_swimming_speed"), 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        // 人鱼们比较幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aquatic_luck_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));

        // 水下挖掘速度增加
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("player.submerged_mining_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aquatic_digging_speed_bonus"), 1.3, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：人鱼之歌 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 75; // 75秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 24.0f; // 24.0 疲劳值 = 6 点饱食度（3个鸡腿）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除生命值（6.0 点 = 3颗心）
        player.hurt(player.damageSources().magic(), 6.0f);
        
        // 2. 扣除饱食度（24.0 疲劳值 = 6 点饱食度 = 3个鸡腿）
        player.getFoodData().addExhaustion(24.0f);
        
        // 3. 查找半径10格内所有玩家
        double radius = 10.0;

        for (Player target : player.level().getEntitiesOfClass(Player.class, 
                player.getBoundingBox().inflate(radius))) {
            // 净化目标的所有效果
            purgeAllEffects(target);
            
            // 给予水下呼吸 III，持续 20 秒（400 ticks）
            target.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 400, 2, false, false, false));
            
            // 给予海豚的恩惠 I，持续 20 秒（400 ticks）
            target.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 400, 0, false, false, false));
            
            // 给予伤害吸收 I（2颗黄心），持续 20 秒（400 ticks）
            target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 0, false, false, false));

        }
        
        // 4. 播放音效（音符盒声音 - 体现歌声）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.NOTE_BLOCK_HARP.value(), player.getSoundSource(), 1.0F, 1.0F);
        
        // 5. 生成粒子效果（附魔粒子 + 水滴粒子）
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = Math.random() * 2.5;
                double offsetZ = (Math.random() - 0.5) * 2.0;
                
                // 交替生成附魔粒子和水滴粒子
                if (i % 2 == 0) {
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                    );
                } else {
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.RAIN,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
    }
    
    /**
     * 净化玩家的所有效果
     */
    private void purgeAllEffects(Player player) {
        // 先收集所有需要移除的效果（避免并发修改异常）
        java.util.List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> effectsToRemove = new java.util.ArrayList<>();
        
        player.getActiveEffects().forEach(effectInstance -> effectsToRemove.add(effectInstance.getEffect()));
        
        // 统一移除所有效果
        for (var effect : effectsToRemove) {
            player.removeEffect(effect);
        }
    }
}
