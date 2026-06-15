package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class Human extends Race {
    
    public Human() {
        super("human", Component.translatable("race.species.human"),
              Component.translatable("race.species.human.short_desc"),
              Component.translatable("race.species.human.benefits"),
              Component.translatable("race.species.human.drawbacks"),
              Component.translatable("skill.species.human.name"),
              Component.translatable("skill.species.human.description"));
    }

    @Override
    protected void initAttributes() {
        // 人族充满了决心，所以比较幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "human_luck_bonus"), 1.0, Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：人之坚毅 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 60; // 60秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 8.0f; // 8.0 疲劳值 = 2 点饱食度（1格/1个鸡腿）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除饱食度（8.0 疲劳值 = 2 点饱食度 = 1格）
        player.getFoodData().addExhaustion(8.0f);
        
        // 2. 给予抗性提升效果，持续 10 秒（200 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, false, false));
        
        // 3. 播放音效（盾牌格挡声 - 体现防御性）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.SHIELD_BLOCK, player.getSoundSource(), 1.0F, 1.0F);
        
        // 4. 生成粒子效果（附魔粒子 - 体现意志力量）
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5);
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5);
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
    }
}
