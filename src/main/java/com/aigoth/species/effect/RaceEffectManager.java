package com.aigoth.species.effect;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.race.Race;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class RaceEffectManager {
    // 存储所有种族效果的映射
    private static final Map<String, List<RaceEffect>> RACE_EFFECTS = new HashMap<>();
    
    static {
        // 初始化各种族的药水效果
        initializeRaceEffects();
    }
    
    // 初始化各种族的药水效果
    private static void initializeRaceEffects() {
        // 猫族效果
        List<RaceEffect> catkinEffects = new ArrayList<>();
        catkinEffects.add(new ConditionalEffect(
            net.minecraft.world.effect.MobEffects.NIGHT_VISION,
            300, 0, false, false, false,
            (player, race, effectsEnabled) -> effectsEnabled
        ));
        RACE_EFFECTS.put("catkin", catkinEffects);
        
        // 粘液族效果
        List<RaceEffect> slimekinEffects = new ArrayList<>();
        slimekinEffects.add(new ConditionalEffect(
            net.minecraft.world.effect.MobEffects.JUMP,
            200, 1, false, false, false,
            (player, race, effectsEnabled) -> effectsEnabled
        ));
        RACE_EFFECTS.put("slimekin", slimekinEffects);
        
        // 植物族效果（无持续药水效果）
        RACE_EFFECTS.put("plantkin", new ArrayList<>());
        
        // 水生族效果（无持续药水效果，完全基于环境）
        RACE_EFFECTS.put("aquatic", new ArrayList<>());
        
        // 犬族效果（无持续药水效果）
        RACE_EFFECTS.put("dogkin", new ArrayList<>());
        
        // 精灵族效果（无持续药水效果，仅在受伤时触发）
        RACE_EFFECTS.put("elf", new ArrayList<>());
        
        // 狐族效果（无持续药水效果，仅在食用浆果时触发）
        RACE_EFFECTS.put("foxkin", new ArrayList<>());
        
        // 羽兽族效果 - 缓降效果（可开关）
        List<RaceEffect> wingedBeastEffects = new ArrayList<>();
        wingedBeastEffects.add(new ConditionalEffect(
            net.minecraft.world.effect.MobEffects.SLOW_FALLING,
            200, 0, false, false, false,
            (player, race, effectsEnabled) -> effectsEnabled // 可开关的效果
        ));
        RACE_EFFECTS.put("wingedbeast", wingedBeastEffects);
        
        // 受诅者效果（无持续药水效果）
        RACE_EFFECTS.put("cursed", new ArrayList<>());
        
        // 人族效果（无特殊药水效果）
        RACE_EFFECTS.put("human", new ArrayList<>());
        
        // 人鱼猫族效果（和猫族一样有夜视效果）
        List<RaceEffect> aigothEffects = new ArrayList<>();
        aigothEffects.add(new ConditionalEffect(
            net.minecraft.world.effect.MobEffects.NIGHT_VISION,
            300, 0, false, false, false,
            (player, race, effectsEnabled) -> effectsEnabled
        ));
        RACE_EFFECTS.put("aigoth", aigothEffects);
    }
    
    // 应用种族效果的统一方法
    public static void applyRaceEffects(Player player, Race race, boolean racialEffectsEnabled) {
        String raceId = race.getRaceId();
        List<RaceEffect> effects = RACE_EFFECTS.get(raceId);
        
        if (effects != null) {
            for (RaceEffect effect : effects) {
                effect.apply(player, race, racialEffectsEnabled);
            }
        }
    }
    
    // 种族效果接口
    public interface RaceEffect {
        void apply(Player player, Race race, boolean racialEffectsEnabled);
    }
    
    // 条件效果记录类
    public record ConditionalEffect(
        net.minecraft.core.Holder<MobEffect> effect,
        int duration,
        int amplifier,
        boolean ambient,
        boolean visible,
        boolean showIcon,
        Condition condition
    ) implements RaceEffect {
        @Override
        public void apply(Player player, Race race, boolean racialEffectsEnabled) {
            if (condition.shouldApply(player, race, racialEffectsEnabled)) {
                // 直接应用效果，不检查现有效果
                player.addEffect(new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon));
            } else {
                // 移除效果（如果是由种族系统添加的）
                MobEffectInstance effectInstance = player.getEffect(effect);
                if (effectInstance != null && !effectInstance.isVisible() 
                    && effectInstance.getAmplifier() == amplifier
                    && effectInstance.getDuration() <= duration) {
                    // 只有当我们确定这是我们添加的效果时才移除
                    // 通过检查放大器和持续时间来确认
                    player.removeEffect(effect);
                }
            }
        }
    }
    
    // 条件接口
    @FunctionalInterface
    public interface Condition {
        boolean shouldApply(Player player, Race race, boolean racialEffectsEnabled);
    }
}