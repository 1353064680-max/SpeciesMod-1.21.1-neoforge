package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Arrow;

public class Elf extends Race {
    
    public Elf() {
        super("elf", Component.translatable("race.species.elf"), 
              Component.translatable("race.species.elf.short_desc"),
              Component.translatable("race.species.elf.benefits"),
              Component.translatable("race.species.elf.drawbacks"),
              Component.translatable("skill.species.elf.name"),
              Component.translatable("skill.species.elf.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值（减少2颗心，即4.0点生命值）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_health_penalty"), -4.0, AttributeModifier.Operation.ADD_VALUE));
        
        // 近战伤害减少2.0点（与远程伤害增强平衡）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_melee_damage_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));
        
        // 减少护甲值2点
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.armor"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_armor_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：凋零之矢 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 80; // 80秒（平衡性调整）
    }
    
    @Override
    public float getFoodExhaustion() {
        return 40.0f; // 40.0 疲劳值 = 10 点饱食度（5格/5个鸡腿）（平衡性调整）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除生命值（2颗心 = 4.0点）
        player.hurt(player.damageSources().magic(), 4.0f);
        
        // 2. 扣除饱食度（40.0 疲劳值 = 10 点饱食度 = 5格）（平衡性调整）
        player.getFoodData().addExhaustion(40.0f);
        
        // 3. 创建箭实体并设置属性
        Arrow arrow = new Arrow(EntityType.ARROW, player.level());
        arrow.setOwner(player);
        
        // 设置箭的位置（从玩家眼睛位置发射）
        arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        
        // 设置箭的方向（与准星瞄准一致）
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
        
        // 设置高伤害（6点基础伤害，配合精灵族x2倍率后为12点）（平衡性调整）
        arrow.setBaseDamage(6.0);
        
        // 设置为无重力（无下坠）
        arrow.setNoGravity(true);
        
        // 设置自定义数据，标记为凋零之矢
        arrow.addTag("wither_arrow");
        
        // 4. 生成箭实体
        player.level().addFreshEntity(arrow);
        
        // 5. 播放音效（弓箭射击声）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.ARROW_SHOOT, player.getSoundSource(), 1.0F, 1.0F);
        
        // 6. 生成粒子效果（魔法粒子）
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = Math.random() * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    player.getX() + offsetX,
                    player.getEyeY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
    }
}
