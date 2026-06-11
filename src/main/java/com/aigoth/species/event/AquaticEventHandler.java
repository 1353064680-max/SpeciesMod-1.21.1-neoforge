package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AquaticEventHandler {
    // 存储水生族玩家离开水的时间
    private static final Map<UUID, Long> OUT_OF_WATER_TIME = new HashMap<>();
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是水生族
        if (!"aquatic".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器处理基础效果
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 获取玩家UUID
        UUID playerUUID = player.getUUID();
        
        if (player.isInWater()) {
            // 玩家在水中
            
            // 立即移除虚弱效果（只移除种族给予的效果）
            removeAquaticEffect(player, MobEffects.WEAKNESS);
            
            // 重置离水时间计时器
            OUT_OF_WATER_TIME.remove(playerUUID);
            
            // 增益效果：水中效果（不受开关控制，不显示图标）
            applyWaterEffects(player);
        } else {
            // 玩家不在水中
            
            // 更新或初始化离水时间计时器
            if (!OUT_OF_WATER_TIME.containsKey(playerUUID)) {
                OUT_OF_WATER_TIME.put(playerUUID, System.currentTimeMillis());
            }
            
            // 检查是否离水超过90秒(90000毫秒)
            Long outOfWaterTime = OUT_OF_WATER_TIME.get(playerUUID);
            if (outOfWaterTime != null) {
                long timeOutOfWater = System.currentTimeMillis() - outOfWaterTime;
                if (timeOutOfWater >= 90000) { // 90秒 = 90000毫秒
                    // 应用虚弱I效果（持续200 ticks，即10秒）
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
                }
                
                // 检查是否离水超过240秒(240000毫秒)
                if (timeOutOfWater >= 240000) { // 240秒 = 240000毫秒
                    // 应用虚弱III与挖掘疲劳II效果（持续200 ticks，即10秒）
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2, false, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1, false, false, false));
                }
            }
            
            // 移除水中效果（只移除种族给予的效果）
            removeAquaticEffect(player, MobEffects.WATER_BREATHING);
            removeAquaticEffect(player, MobEffects.CONDUIT_POWER);
            removeAquaticEffect(player, MobEffects.DOLPHINS_GRACE);
        }
    }
    
    private static void applyWaterEffects(Player player) {
        // 水中效果不受种族药水效果开关控制，且不显示图标
        applyAquaticEffect(player, MobEffects.WATER_BREATHING);
        applyAquaticEffect(player, MobEffects.CONDUIT_POWER);
        applyAquaticEffect(player, MobEffects.DOLPHINS_GRACE);
    }
    
    private static void applyAquaticEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 正确使用三个false参数
        player.addEffect(new MobEffectInstance(effect, 200, 0, false, false, false));
    }
    
    private static void removeAquaticEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 只移除由种族系统添加的效果（通过检查效果的可见性来判断是否为种族系统添加的）
        MobEffectInstance effectInstance = player.getEffect(effect);
        if (effectInstance != null && !effectInstance.isVisible()) {
            player.removeEffect(effect);
        }
    }
}