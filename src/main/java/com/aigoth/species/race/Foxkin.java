package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Foxkin extends Race {
    public Foxkin() {
        super("foxkin", Component.translatable("race.species.foxkin"),
              Component.translatable("race.species.foxkin.short_desc"),
              Component.translatable("race.species.foxkin.benefits"),
              Component.translatable("race.species.foxkin.drawbacks"),
              Component.translatable("skill.species.foxkin.name"),
              Component.translatable("skill.species.foxkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_health_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加移动速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_speed_bonus"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        // 增加摔落缓冲
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_fall_resistance_bonus"), 2.0, AttributeModifier.Operation.ADD_VALUE));
    }
}