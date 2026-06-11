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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Aigoth extends Race {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public Aigoth() {
        super("aigoth", Component.translatable("race.species.aigoth"), 
              Component.translatable("race.species.aigoth.short_desc"),
              Component.translatable("race.species.aigoth.benefits"),
              Component.translatable("race.species.aigoth.drawbacks"),
              Component.translatable("skill.species.aigoth.name"),
              Component.translatable("skill.species.aigoth.description"));
    }

    @Override
    protected void initAttributes() {
        // 水生族属性：增加游泳速度
        attributeModifiers.put(ResourceLocation.fromNamespaceAndPath("neoforge", "swim_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_swimming_speed"), 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        // 水生族属性：增加幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_luck_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
        
        // 增加摔落缓冲距离（8格以下免疫）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_fall_resistance_bonus"), 8.0, AttributeModifier.Operation.ADD_VALUE));

        //水下挖掘速度增加
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("player.submerged_mining_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_mining_speed_bonus"), 1.3, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：哈气（与猫族相同）==========
    
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
        LOGGER.debug("玩家 {} 使用哈气技能，扣除 24.0 疲劳值（3个鸡腿）", player.getName().getString());
        
        // 2. 给予玩家自身增益效果（力量I + 速度I，持续10秒）
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, false, false)); // 力量I
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false, false)); // 速度I
        LOGGER.debug("玩家 {} 获得力量I和速度I效果，持续10秒", player.getName().getString());
        
        // 3. 播放猫哈气音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.CAT_HISS, player.getSoundSource(), 1.0F, 1.0F);
        LOGGER.debug("播放猫哈气音效");
        
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
        LOGGER.debug("生成愤怒村民粒子效果");
        
        // 5. 查找半径3格内所有生物并施加减益效果
        double radius = 3.0;
        int affectedCount = 0;
        
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(radius))) {
            // 排除自己
            if (entity == player) continue;
            
            // 给予虚弱I和缓慢I，持续10秒
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, false, false));
            affectedCount++;
        }
        
        LOGGER.info("哈气技能影响范围内 {} 个生物，施加虚弱I和缓慢I效果", affectedCount);
    }
}