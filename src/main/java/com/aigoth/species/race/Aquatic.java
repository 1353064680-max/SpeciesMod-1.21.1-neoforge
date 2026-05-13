package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class Aquatic extends Race {
    public Aquatic() {
        super("aquatic", Component.translatable("race.species.aquatic"), 
              Component.translatable("race.species.aquatic.short_desc"),
              Component.translatable("race.species.aquatic.desc"));
    }

    @Override
    protected void initAttributes() {
        // 增加游泳速度
        attributeModifiers.put(ResourceLocation.fromNamespaceAndPath("neoforge", "swim_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aquatic_swimming_speed"), 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        // 人鱼们比较幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aquatic_luck_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
    }
}