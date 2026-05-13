package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class WingedBeastEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是羽兽族
        if (!"wingedbeast".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器应用可开关的效果（如缓降）
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 处理水中负面效果（环境效果，不受开关控制）
        handleWaterEffects(player);
    }
    
    // 处理羽兽族在水中的负面效果（环境效果，不受开关控制）
    private static void handleWaterEffects(Player player) {
        // 判断玩家是否在水中或在雨中
        Level level = player.level();
        if (player.isInWater() || (level.isRaining() && level.canSeeSky(player.blockPosition()))) {
            // 给予虚弱和缓慢效果（环境效果，始终应用）
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, false, false));
        }
    }
    
    // 处理羽兽族睡醒下蛋逻辑
    public static void handlePlayerWakeUp(Player player) {
        // 玩家睡醒时必定生成一个鸡蛋
        player.spawnAtLocation(new ItemStack(Items.EGG), 1.0f);
    }
    
    // 羽兽族受到火焰伤害1.5倍
    public static float applyFireDamageMultiplier(float originalDamage) {
        return originalDamage * 1.5f;
    }
}