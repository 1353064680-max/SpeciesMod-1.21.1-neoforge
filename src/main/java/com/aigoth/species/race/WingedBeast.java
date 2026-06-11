package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.event.WingedBeastEventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WingedBeast extends Race {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public WingedBeast() {
        super("wingedbeast", Component.translatable("race.species.wingedbeast"),
              Component.translatable("race.species.wingedbeast.short_desc"),
              Component.translatable("race.species.wingedbeast.benefits"),
              Component.translatable("race.species.wingedbeast.drawbacks"),
              Component.translatable("skill.species.wingedbeast.name"),
              Component.translatable("skill.species.wingedbeast.description"));
    }

    @Override
    protected void initAttributes() {
        // 增加移速
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "wingedbeast_speed_bonus"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    
    // ========== 主动技能：振翅飞翔 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 120; // 120秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 48.0f; // 48.0 疲劳值 = 12 点饱食度（6格/6个鸡腿）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除饱食度（48.0 疲劳值 = 12 点饱食度 = 6格）
        player.getFoodData().addExhaustion(48.0f);
        LOGGER.debug("玩家 {} 使用振翅飞翔，扣除 48.0 疲劳值（6格饱食度）", player.getName().getString());
        
        // 2. 给予创造模式飞行能力，持续 20 秒（400 ticks）
        player.getAbilities().flying = true;
        //noinspection deprecation
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        
        // 设置飞行能力到期时间（当前tick + 400 ticks = 20秒）
        long expiryTick = player.level().getGameTime() + 400;
        WingedBeastEventHandler.setFlightExpiry(player, expiryTick);
        LOGGER.debug("玩家 {} 获得创造飞行能力，持续 20 秒", player.getName().getString());
        
        // 3. 给予虚弱 I 效果，持续 20 秒（400 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0, false, false, false));
        LOGGER.debug("玩家 {} 获得虚弱I效果，持续 20 秒", player.getName().getString());
        
        // 4. 播放音效（翅膀拍打声 - 使用幻翼音效）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.PHANTOM_FLAP, player.getSoundSource(), 1.0F, 1.0F);
        LOGGER.debug("播放翅膀拍打音效");
        
        // 5. 生成粒子效果（羽毛/风粒子）
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 1.5;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * 1.5;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
        LOGGER.debug("生成振翅飞翔粒子效果");
    }
}
