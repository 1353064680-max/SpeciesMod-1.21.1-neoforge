package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class CatkinEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是猫族
        if (!"catkin".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
    }
    
    // 当实体加入世界时，为苦力怕添加避开猫族玩家的目标
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否是苦力怕并且不是客户端世界
        if (event.getEntity() instanceof Creeper creeper && !event.getLevel().isClientSide()) {
            // 为苦力怕添加避开猫族玩家的AI目标
            // 与猫的实现类似，苦力怕会避开猫族玩家（距离10格内，逃离速度1.5）
            creeper.goalSelector.addGoal(2, new AvoidEntityGoal<>(creeper, Player.class, 10.0F, 1.5D, 1.5D,
                entity -> entity instanceof Player && isCatkinPlayer((Player) entity)));
        }
    }
    
    // 检查玩家是否为猫族
    private static boolean isCatkinPlayer(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        return cap.hasSelectedRace() && "catkin".equals(cap.getSelectedRace().getRaceId());
    }
    
    public static void handleFallDamage(net.minecraft.world.entity.Entity entity, float distance, float multiplier) {
        // 通过修改摔落距离来提高摔落伤害的起始高度阈值
        // 原版阈值是3格，猫族增加到8格（增加5格）
        // 如果摔落距离小于等于8格，则不造成伤害
        if (distance <= 8.0f && entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            livingEntity.fallDistance = 0;
        }
    }
}