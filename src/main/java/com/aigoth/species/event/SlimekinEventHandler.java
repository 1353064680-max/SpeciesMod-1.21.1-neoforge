package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public class SlimekinEventHandler {


    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是粘液族
        if (!"slimekin".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 检查是否在干旱生物群系中（沙漠、恶地等）
        Biome biome = player.level().getBiome(player.blockPosition()).value();
        Biome.Precipitation precipitation = biome.getPrecipitationAt(player.blockPosition());
        
        // 如果在干旱生物群系中（无降水），应用虚弱和缓慢效果
        if (precipitation == Biome.Precipitation.NONE) {
            // 应用虚弱II效果
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, false, false));
            // 应用缓慢II效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false, false));
        } else {
            // 移除虚弱和缓慢效果（只移除种族给予的效果）
            removeSlimekinEffect(player, MobEffects.WEAKNESS);
            removeSlimekinEffect(player, MobEffects.MOVEMENT_SLOWDOWN);
        }
    }
    
    private static void removeSlimekinEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 只移除由种族系统添加的效果（通过检查效果的可见性来判断是否为种族系统添加的）
        MobEffectInstance effectInstance = player.getEffect(effect);
        if (effectInstance != null && !effectInstance.isVisible()) {
            player.removeEffect(effect);
        }
    }
    
    // 处理粘液族攻击时给目标添加缓慢效果
    public static void handlePlayerAttack(LivingEntity target) {
        // 给攻击目标添加缓慢II效果，持续2秒（40 ticks）
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, false));
    }
}