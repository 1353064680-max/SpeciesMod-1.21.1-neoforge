package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CursedEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是受诅者
        if (!"cursed".equals(race.getRaceId())) {
            return;
        }
        // 使用统一的药水效果管理器
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        // 获取世界对象
        var level = player.level();
        
        // 检查是否在阳光下（被动效果，始终生效）
        if (level.isDay() && !level.isRaining() && level.canSeeSky(player.blockPosition())) {
            // 添加着火效果（持续2秒）
            player.setRemainingFireTicks(40); // 20 ticks = 1秒，所以40 ticks = 2秒
        }
        
        // 检查是否在月光下（夜晚且能看到天空）（被动效果，始终生效）
        if (!level.isDay() && level.canSeeSky(player.blockPosition())) {
            // 添加凋零I效果（持续200 ticks，即10秒）
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0, false, false, false));
        }
        
        // 检查生命值是否低于最大生命值的一半，如果是则获得永久黑暗效果（不可被按钮关闭）
        if (player.getHealth() < player.getMaxHealth() / 2) {
            // 添加黑暗效果（持续200 ticks，会不断刷新）
            // 第5个参数设为true表示显示药水图标，第6个参数设为false表示不显示粒子效果
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, true, false));
        }
        
        // 注意：受诅族的所有效果都是被动特性，不受racialEffectsEnabled参数控制
        // 这符合被动种族特性应用规范：被动种族特性必须始终保持开启状态
    }
    
    public static void handlePlayerAttack(Player player, LivingEntity target) {
        // 参数player保留以维持接口一致性
        // 给攻击目标添加凋零I效果，持续2秒（40 ticks）
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, false, false));
    }
}