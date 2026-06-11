package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class Catkin extends Race {
    public Catkin() {
        super("catkin", Component.translatable("race.species.catkin"),
              Component.translatable("race.species.catkin.short_desc"),
              Component.translatable("race.species.catkin.desc"),
              Component.translatable("race.species.catkin.benefits"),
              Component.translatable("race.species.catkin.drawbacks"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_health_penalty"), -2.0, Operation.ADD_VALUE));
        
        // 减少攻击伤害
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_attack_damage_penalty"), -4.0, Operation.ADD_VALUE));

        // 增加速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_speed_bonus"), 0.2, Operation.ADD_MULTIPLIED_TOTAL));
        
        // 增加摔落缓冲距离（8格以下免疫）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_fall_resistance_bonus"), 8.0, Operation.ADD_VALUE));

        // 减少攻击速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "catkin_attack_speed_penalty"), -0.5, Operation.ADD_VALUE));
    }
}