package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.effect.RaceEffectManager;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

public class AigothEventHandler {
    
    public static void applyRacialEffects(Player player, Race race, boolean racialEffectsEnabled) {
        // 确保传入的是人鱼猫族
        if (!"aigoth".equals(race.getRaceId())) {
            return;
        }
        
        // 使用统一的药水效果管理器
        RaceEffectManager.applyRaceEffects(player, race, racialEffectsEnabled);
        
        // 人鱼猫族在水中获得与水生族相同地增益效果
        if (player.isInWater()) {
            applyWaterEffects(player);
        } else {
            // 移除水中效果（只移除种族给予的效果）
            removeAigothEffect(player, net.minecraft.world.effect.MobEffects.WATER_BREATHING);
            removeAigothEffect(player, net.minecraft.world.effect.MobEffects.DIG_SPEED);
            removeAigothEffect(player, net.minecraft.world.effect.MobEffects.CONDUIT_POWER);
            removeAigothEffect(player, net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE);
        }
    }
    
    // 当实体加入世界时，为苦力怕添加避开人鱼猫族玩家的目标
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否是苦力怕并且不是客户端世界
        if (event.getEntity() instanceof Creeper creeper && !event.getLevel().isClientSide()) {
            // 为苦力怕添加避开人鱼猫族玩家的AI目标
            // 与猫的实现类似，苦力怕会避开人鱼猫族玩家（距离10格内，逃离速度1.5）
            creeper.goalSelector.addGoal(2, new AvoidEntityGoal<>(creeper, Player.class, 10.0F, 1.5D, 1.5D,
                entity -> entity instanceof Player && isAigothPlayer((Player) entity)));
        }
    }
    
    // 检查玩家是否为人鱼猫族
    private static boolean isAigothPlayer(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        return cap.hasSelectedRace() && "aigoth".equals(cap.getSelectedRace().getRaceId());
    }
    
    // 应用水中效果
    private static void applyWaterEffects(Player player) {
        // 水中效果不受种族药水效果开关控制，且不显示图标
        applyAigothEffect(player, net.minecraft.world.effect.MobEffects.WATER_BREATHING);
        applyAigothEffect(player, net.minecraft.world.effect.MobEffects.DIG_SPEED);
        applyAigothEffect(player, net.minecraft.world.effect.MobEffects.CONDUIT_POWER);
        applyAigothEffect(player, net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE);
    }
    
    private static void applyAigothEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 正确使用三个false参数
        player.addEffect(new MobEffectInstance(effect, 200, 0, false, false, false));
    }
    
    private static void removeAigothEffect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        // 只移除由种族系统添加的效果（通过检查效果的可见性来判断是否为种族系统添加的）
        MobEffectInstance effectInstance = player.getEffect(effect);
        if (effectInstance != null && !effectInstance.isVisible()) {
            player.removeEffect(effect);
        }
    }
    
    // 处理人鱼猫族增加摔落阈值
    public static void handleFallDamage(net.minecraft.world.entity.Entity entity, float distance, float multiplier) {
        // 通过修改摔落距离来提高摔落伤害的起始高度阈值
        // 原版阈值是3格，人鱼猫族增加到8格（增加5格）
        // 如果摔落距离小于等于8格，则不造成伤害
        if (distance <= 8.0f && entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            livingEntity.fallDistance = 0;
        }
    }
}