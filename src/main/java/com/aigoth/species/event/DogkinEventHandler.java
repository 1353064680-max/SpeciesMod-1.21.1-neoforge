package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class DogkinEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保是犬族玩家
        if (!"dogkin".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器处理基础效果
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        Level world = player.level();
        
        // 检查是否为满月（被动效果，始终生效）
        if (world.isNight() && world.getMoonPhase() == 0) { // 满月的月相值为0
            // 满月时获得力量I和饥饿I效果（始终生效）
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0, false, false, false));
        }
        // 注意：不再主动移除月相效果，让其自然过期
        // 这样可以避免误清除技能给予的相同类型效果
        
        // 检查是否为月食（新月时的月食）（被动效果，始终生效）
        if (world.isNight() && world.getMoonPhase() == 4) {
            // 月食时获得虚弱I和抗性提升I效果（始终生效）
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, false, false));
        }
        // 注意：不再主动移除月食效果，让其自然过期
        // 这样可以避免误清除技能给予的相同类型效果
        
        // 注意：犬族的所有效果都是被动特性，不受racialEffectsEnabled参数控制
        // 该参数保留以维持接口一致性
    }
    
    // 当实体加入世界时，为骷髅类生物添加避开犬族玩家的目标
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否是骷髅类生物并且不是客户端世界
        Entity entity = event.getEntity();
        if ((entity instanceof Skeleton || entity instanceof WitherSkeleton || entity instanceof Stray) 
                && !event.getLevel().isClientSide()) {
            // 为骷髅类生物添加避开犬族玩家的AI目标
            // 骷髅类生物会避开犬族玩家（距离10格内，逃离速度1.5）
            if (entity instanceof Skeleton skeleton) {
                skeleton.goalSelector.addGoal(3, new AvoidEntityGoal<>(skeleton, Player.class, 10.0F, 1.5D, 1.5D,
                    player -> isDogkinPlayer((Player) player)));
            } else if (entity instanceof WitherSkeleton witherSkeleton) {
                witherSkeleton.goalSelector.addGoal(3, new AvoidEntityGoal<>(witherSkeleton, Player.class, 10.0F, 1.5D, 1.5D,
                    player -> isDogkinPlayer((Player) player)));
            } else if (entity instanceof Stray stray) {
                stray.goalSelector.addGoal(3, new AvoidEntityGoal<>(stray, Player.class, 10.0F, 1.5D, 1.5D,
                    player -> isDogkinPlayer((Player) player)));
            }
        }
    }
    
    // 检查玩家是否为犬族
    private static boolean isDogkinPlayer(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        return cap.hasSelectedRace() && "dogkin".equals(cap.getSelectedRace().getRaceId());
    }
    
    // 处理犬族饱食度消耗增加的逻辑
    public static void onPlayerTick(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (cap.hasSelectedRace() && "dogkin".equals(cap.getSelectedRace().getRaceId())) {
            // 增加饱食度消耗
            FoodData foodData = player.getFoodData();
            // 每秒有30%概率额外增加0.5点疲劳值
            if (player.tickCount % 20 == 0 && Math.random() < 0.3) {
                foodData.addExhaustion(0.5f);
            }
        }
    }
}