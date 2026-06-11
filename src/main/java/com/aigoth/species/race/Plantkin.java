package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Plantkin extends Race {
    public Plantkin() {
        super("plantkin", Component.translatable("race.species.plantkin"),
              Component.translatable("race.species.plantkin.short_desc"),
              Component.translatable("race.species.plantkin.benefits"),
              Component.translatable("race.species.plantkin.drawbacks"),
              Component.translatable("skill.species.plantkin.name"),
              Component.translatable("skill.species.plantkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 增加最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "plantkin_health_bonus"), 4.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加击退抗性
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.knockback_resistance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "plantkin_knockback_resistance_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
    }
}