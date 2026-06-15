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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Plantkin extends Race {
    
    public Plantkin() {
        super("plantkin", Component.translatable("race.species.plantkin"),
              Component.translatable("race.species.plantkin.short_desc"),
              Component.translatable("race.species.plantkin.benefits"),
              Component.translatable("race.species.plantkin.drawbacks"),
              Component.translatable("skill.species.plantkin.name"),
              Component.translatable("skill.species.plantkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 增加最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "plantkin_health_bonus"), 4.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加击退抗性
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.knockback_resistance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "plantkin_knockback_resistance_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：自然恩泽 ==========
    
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
        return 0.0f; // 不消耗饱食度
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 检查并扣除经验（1级）
        int currentExp = player.experienceProgress >= 0 ? 
            (int)(player.experienceProgress * player.getXpNeededForNextLevel()) : 0;
        int totalLevels = player.experienceLevel;
        
        if (totalLevels < 1 && currentExp < player.getXpNeededForNextLevel()) {
            // 经验为0，无法使用技能
            player.sendSystemMessage(Component.translatable("skill.species.no_resource_exp"));
            return;
        }
        
        // 扣除1级经验
        player.giveExperienceLevels(-1);
        
        // 2. 查找半径5格内所有玩家
        double radius = 5.0;

        for (Player target : player.level().getEntitiesOfClass(Player.class, 
                player.getBoundingBox().inflate(radius))) {
            
            // 给予生命恢复 III 效果，持续 10 秒（200 ticks）
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, false));
            
            // 给予饱和效果，持续 10 秒（200 ticks）
            target.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200, 0, false, false, false));

        }

        
        // 3. 给范围内的每个玩家一个发光浆果
        ItemStack glowBerry = new ItemStack(Items.GLOW_BERRIES, 1);
        
        for (Player target : player.level().getEntitiesOfClass(Player.class, 
                player.getBoundingBox().inflate(radius))) {
            // 尝试将发光浆果添加到玩家背包
            boolean added = target.getInventory().add(glowBerry.copy());
            
            if (!added) {
                // 如果背包满了，掉落在地上
                target.drop(glowBerry.copy(), false);
            }
        }

        
        // 4. 播放音效（自然/治愈音效 - 使用附魔台音效）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 1.0F, 1.0F);
        
        // 5. 生成粒子效果（绿色魔法粒子）
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * radius * 2;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * radius * 2;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
    }
}
