package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlantkinEventHandler {
    // 存储植物族玩家上一次获得饱和效果的时间
    private static final Map<UUID, Long> LAST_SATURATION_TIME = new HashMap<>();
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是植物族
        if (!"plantkin".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器处理基础效果
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        Level world = player.level();
        int lightLevel = world.getLightEngine().getRawBrightness(player.blockPosition(), 0);
        boolean isDaytime = world.isDay();
        
        // 植物族效果逻辑：
        // 1. 白天且光照充足（光照等级>=10）：获得增益效果
        // 2. 夜晚且光照不足（光照等级<10）：受到减益效果
        // 3. 其他情况（白天光照不足、夜晚光照充足）：无增益也无减益
        
        if (isDaytime) {
            // 白天：根据光照判断
            if (lightLevel >= 10) {
                applyBeneficialEffects(player, racialEffectsEnabled);
            } else {
                removePlantkinEffects(player, racialEffectsEnabled);
            }
        } else {
            // 夜晚：根据光照判断
            if (lightLevel < 10) {
                applyHarmfulEffects(player, racialEffectsEnabled);
            } else {
                removePlantkinEffects(player, racialEffectsEnabled);
            }
        }
    }
    
    // 应用有益效果（白天且光照充足时）
    private static void applyBeneficialEffects(Player player, boolean racialEffectsEnabled) {
        if (racialEffectsEnabled) {
            // 光合作用回血效果
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, false, false));
            
            // 每20秒给予1 tick的饱和效果
            UUID uuid = player.getUUID();
            long currentTime = System.currentTimeMillis();
            Long lastSaturationTime = LAST_SATURATION_TIME.get(uuid);
            
            if (lastSaturationTime == null || currentTime - lastSaturationTime >= 20000) { // 20秒 = 20000毫秒
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, false, false,  false));
                LAST_SATURATION_TIME.put(uuid, currentTime);
            }
        } else {
            // 移除光合作用回血效果（只移除种族给予的效果）
            removePlantkinEffect(player, MobEffects.REGENERATION);
            removePlantkinEffect(player, MobEffects.SATURATION);
        }
        
        // 移除减益效果（只移除种族给予的效果）
        removePlantkinEffect(player, MobEffects.WEAKNESS);
    }
    
    // 应用有害效果（夜晚且光照不足时）
    private static void applyHarmfulEffects(Player player, boolean racialEffectsEnabled) {
        // 移除增益效果（只移除种族给予的效果）
        if (racialEffectsEnabled) {
            removePlantkinEffect(player, MobEffects.REGENERATION);
            removePlantkinEffect(player, MobEffects.SATURATION);
        }
        
        // 应用减益效果（虚弱）
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
    }
    
    // 移除所有植物族效果
    private static void removePlantkinEffects(Player player, boolean racialEffectsEnabled) {
        // 移除所有植物族效果
        if (racialEffectsEnabled) {
            removePlantkinEffect(player, MobEffects.REGENERATION);
            removePlantkinEffect(player, MobEffects.SATURATION);
        }
        removePlantkinEffect(player, MobEffects.WEAKNESS);
    }
    
    // 处理植物族在夜晚或黑暗中受伤时获得饥饿效果
    public static void handleHurtEvent(Player player) {
        Level world = player.level();
        int lightLevel = world.getLightEngine().getRawBrightness(player.blockPosition(), 0);
        boolean isNightOrDark = !world.isDay() || lightLevel < 8;
        
        // 检查是否在夜晚或黑暗中
        if (isNightOrDark) {
            // 添加饥饿II效果，持续4秒（80 ticks）
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1, false, false, false));
        }
    }
    
    private static void removePlantkinEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 只移除由种族系统添加的效果（通过检查效果的可见性来判断是否为种族系统添加的）
        MobEffectInstance effectInstance = player.getEffect(effect);
        if (effectInstance != null && !effectInstance.isVisible()) {
            player.removeEffect(effect);
        }
    }
}