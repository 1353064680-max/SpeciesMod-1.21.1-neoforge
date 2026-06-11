package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Slimekin extends Race {
    public Slimekin() {
        super("slimekin", Component.translatable("race.species.slimekin"),
              Component.translatable("race.species.slimekin.short_desc"),
              Component.translatable("race.species.slimekin.benefits"),
              Component.translatable("race.species.slimekin.drawbacks"),
              Component.translatable("skill.species.slimekin.name"),
              Component.translatable("skill.species.slimekin.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_health_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加击退抗性
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.knockback_resistance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_knockback_resistance_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
        
        // 完全免疫摔落伤害（设置极大的安全摔落距离）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_fall_immunity"), 1024.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加跳跃高度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.jump_strength"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "slimekin_jump_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
    }
}