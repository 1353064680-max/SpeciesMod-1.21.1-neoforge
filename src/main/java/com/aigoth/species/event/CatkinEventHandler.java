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
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Creeper creeper) {
            // 借鉴 Artifacts 方案：除了事件拦截外，保留 AI 目标以实现主动规避视觉效果
            // 原版猫驱散苦力怕的逻辑：半径 6 格，速度倍数 1.5, 1.5
            creeper.goalSelector.addGoal(3, new AvoidEntityGoal<>(creeper, Player.class, 6.0F, 1.5D, 1.5D,
                entity -> isCatkinPlayer((Player) entity)));
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