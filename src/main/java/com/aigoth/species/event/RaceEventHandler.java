package com.aigoth.species.event;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public class RaceEventHandler {
    
    // ================================
    // 摔落事件处理
    // ================================
    
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace() && cap.isRacialEffectsEnabled()) {
                Race race = cap.getSelectedRace();
                reduceFallDamageByRace(event, race);
            }
        }
    }
    
    private static void reduceFallDamageByRace(LivingFallEvent event, Race race) {
        String raceId = race.getRaceId();
        
        switch (raceId) {
            case "catkin":
                CatkinEventHandler.handleFallDamage(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
                break;
                
            case "slimekin":
                SlimekinEventHandler.handleFallDamage(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
                break;
                
            case "aigoth":
                AigothEventHandler.handleFallDamage(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
                break;
                
            default:
                break;
        }
    }
    
    // ================================
    // 受伤事件处理
    // ================================
    
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            // 狐族免疫浆果丛伤害
            if (cap.hasSelectedRace() && "foxkin".equals(cap.getSelectedRace().getRaceId())) {
                if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                    event.setNewDamage(0);
                    return;
                }
            }
            
            // 植物族夜间受伤时获得饥饿效果
            if (cap.hasSelectedRace() && "plantkin".equals(cap.getSelectedRace().getRaceId())) {
                PlantkinEventHandler.handleHurtEvent(player);
            }
            
            // 精灵族受到伤害后获得速度效果
            if (cap.hasSelectedRace() && "elf".equals(cap.getSelectedRace().getRaceId())) {
                ElfEventHandler.onLivingHurt(event);
            }
            
            // 常驻效果（不受开关影响）
            if (cap.hasSelectedRace()) {
                if ("plantkin".equals(cap.getSelectedRace().getRaceId())) {
                    // 植物族受到火焰伤害翻倍
                    if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                        event.setNewDamage(event.getNewDamage() * 2.0f);
                    }
                } else if ("wingedbeast".equals(cap.getSelectedRace().getRaceId())) {
                    // 羽兽族受到火焰伤害1.5倍
                    if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                        event.setNewDamage(WingedBeastEventHandler.applyFireDamageMultiplier(event.getNewDamage()));
                    }
                }
            }
        }
    }
    
    // ================================
    // 物品使用事件处理
    // ================================
    
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            // 狐族吃浆果获得生命恢复效果（常驻效果）
            if (cap.hasSelectedRace() && "foxkin".equals(cap.getSelectedRace().getRaceId())) {
                ItemStack item = event.getItem();
                FoxkinEventHandler.handleItemUseFinish(player, item);
            }
        }
    }
    
    // ================================
    // 攻击事件处理
    // ================================
    
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (cap.hasSelectedRace()) {
            if ("slimekin".equals(cap.getSelectedRace().getRaceId())) {
                if (event.getTarget() instanceof LivingEntity livingTarget) {
                    SlimekinEventHandler.handlePlayerAttack(livingTarget);
                }
            } else if ("cursed".equals(cap.getSelectedRace().getRaceId())) {
                if (event.getTarget() instanceof LivingEntity livingTarget) {
                    CursedEventHandler.handlePlayerAttack(player, livingTarget);
                }
            }
        }
    }
    
    // ================================
    // 玩家更新事件处理
    // ================================
    
    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            // 犬族饱食度消耗逻辑
            DogkinEventHandler.onPlayerTick(player);
            
            // 羽兽族在水中的效果（常驻效果）
            if (player.tickCount % 20 == 0) {
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                if (cap.hasSelectedRace() && "wingedbeast".equals(cap.getSelectedRace().getRaceId())) {
                    WingedBeastEventHandler.applyRacialEffects(player, cap.getSelectedRace(), true);
                }
            }
        }
    }
    
    // ================================
    // 睡眠事件处理
    // ================================
    
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        // 羽兽族睡醒下蛋逻辑
        if (cap.hasSelectedRace() && "wingedbeast".equals(cap.getSelectedRace().getRaceId())) {
            WingedBeastEventHandler.handlePlayerWakeUp(player);
        }
    }
    
    // ================================
    // 实体生成事件处理
    // ================================
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 猫族苦力怕避开逻辑
        CatkinEventHandler.onEntityJoinWorld(event);
        
        // 人鱼猫族苦力怕避开逻辑
        AigothEventHandler.onEntityJoinWorld(event);
        
        // 犬族骷髅类生物避开逻辑
        DogkinEventHandler.onEntityJoinWorld(event);
        
        // 精灵族箭矢增强
        ElfEventHandler.onEntityJoinLevel(event);
    }
    
    // ================================
    // 效果应用
    // ================================
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 使用统一的药水效果管理器应用种族效果
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 应用特殊效果
        String raceId = race.getRaceId();

        switch (raceId) {
            case "catkin":
                CatkinEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
                
            case "foxkin":
                FoxkinEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;

            case "aquatic":
                AquaticEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;

            case "slimekin":
                SlimekinEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;

            case "plantkin":
                PlantkinEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;

            case "cursed":
                CursedEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
                
            case "wingedbeast":
                WingedBeastEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
                
            case "dogkin":
                DogkinEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
                
            case "elf":
                ElfEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
                
            case "aigoth":
                AigothEventHandler.applyRacialEffects(player, race, racialEffectsEnabled);
                break;
        }
    }
}