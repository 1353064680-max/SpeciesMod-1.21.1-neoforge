package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public class RaceEventHandler {
    
    // ================================
    // 受伤事件处理
    // ================================
    
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            
            // 狐族免疫浆果丛伤害
            if (cap.hasSelectedRace() && "foxkin".equals(cap.getSelectedRace().getRaceId())) {
                // 在 1.21.1 中，通过 DamageSource 的 msgId 或注册 ID 判断
                // 尝试使用 is 方法检查标签（如果存在）或直接比对 ID
                if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.SWEET_BERRY_BUSH)) {
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
            
            // 猫族/人鱼猫族二段跳落地重置逻辑（服务端）
            resetDoubleJumpOnLand(player);

            // 羽兽族在水中的效果（常驻效果）
            if (player.tickCount % 20 == 0) {
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                if (cap.hasSelectedRace() && "wingedbeast".equals(cap.getSelectedRace().getRaceId())) {
                    WingedBeastEventHandler.applyRacialEffects(player, cap.getSelectedRace(), true);
                }
            }
        }
    }
    
    private static void resetDoubleJumpOnLand(Player player) {
        if (player.level().isClientSide) return;
        if (!player.onGround()) return;
        
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (!cap.hasSelectedRace()) return;
        
        String raceId = cap.getSelectedRace().getRaceId();
        if (!"catkin".equals(raceId) && !"aigoth".equals(raceId) && !"wingedbeast".equals(raceId)) return;
        
        com.aigoth.species.network.DoubleJumpRequestPacket.resetOnLand(player.getUUID());
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
    // 目标选择事件处理 (借鉴 Artifacts 方案)
    // ================================
    
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // 仅在服务端处理，确保权威性
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof net.minecraft.world.entity.monster.Creeper) {
            if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                if (cap.hasSelectedRace() && ("catkin".equals(cap.getSelectedRace().getRaceId()) || "aigoth".equals(cap.getSelectedRace().getRaceId()))) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // ================================
    // 幻翼驱散逻辑 (借鉴 Artifacts 方案)
    // ================================
    
    @SubscribeEvent
    public static void onPhantomTick(EntityTickEvent.Pre event) {
        // 仅在服务端处理
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof Phantom phantom) {
            LivingEntity target = phantom.getTarget();
            if (target instanceof Player player && phantom.distanceToSqr(player) < 256.0) { // 16格半径
                PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                if (cap.hasSelectedRace() && ("catkin".equals(cap.getSelectedRace().getRaceId()) || "aigoth".equals(cap.getSelectedRace().getRaceId()))) {
                    // 播放猫叫嘶吼声并清除目标
                    phantom.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                        net.minecraft.sounds.SoundEvents.CAT_HISS, player.getSoundSource(), 1.0F, 1.0F);
                    phantom.setTarget(null);
                }
            }
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