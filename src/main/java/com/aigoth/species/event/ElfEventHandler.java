package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

public class ElfEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保是精灵族
        if (!"elf".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器处理基础效果
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 方法体可以为空，因为精灵族的效果已经在其他事件处理器中实现
    }
    
    // 处理精灵族受到伤害后获得速度效果
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace() && "elf".equals(cap.getSelectedRace().getRaceId())) {
                // 受到伤害后获得速度II效果5秒（100 ticks）
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false, false));
            }
        }
    }
    
    // 处理精灵族箭矢伤害翻倍
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // 检查是否为箭矢实体
        if (event.getEntity() instanceof AbstractArrow arrow) {
            // 检查箭矢是否由玩家射出，并且玩家是精灵族
            if (arrow.getOwner() instanceof Player player) {
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                if (cap.hasSelectedRace() && "elf".equals(cap.getSelectedRace().getRaceId())) {
                    // 将基础伤害翻倍（倍率加成）
                    arrow.setBaseDamage(arrow.getBaseDamage() * 2.0);
                }
            }
        }
    }
    
    // 处理凋零之矢命中事件
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭矢
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            // 检查是否是凋零之矢（通过 tag 标记）
            if (arrow.getTags().contains("wither_arrow")) {
                // 检查命中目标是否为生物
                if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                    if (entityHitResult.getEntity() instanceof LivingEntity target) {
                        // 施加凋零 I 效果，持续 5 秒（100 ticks）（平衡性调整）
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, false, true));
                    }
                }
            }
        }
    }
}
