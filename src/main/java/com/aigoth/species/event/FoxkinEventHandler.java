package com.aigoth.species.event;

import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoxkinEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是狐族
        if (!"foxkin".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
    }
    
    // 处理狐族吃浆果获得生命恢复效果
    public static void handleItemUseFinish(Player player, ItemStack item) {
        // 检查是否为甜浆果
        if (item.getItem() == Items.SWEET_BERRIES) {
            // 给予生命恢复效果，等级1，持续4秒（80 ticks）
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, false, false, false));
        }
    }
}