package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class Human extends Race {
    public Human() {
        super("human", Component.translatable("race.species.human"),
              Component.translatable("race.species.human.short_desc"),
              Component.translatable("race.species.human.desc"),
              Component.translatable("race.species.human.benefits"),
              Component.translatable("race.species.human.drawbacks"));
    }

    @Override
    protected void initAttributes() {
        // 人族充满了决心，所以比较幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "human_luck_bonus"), 1.0, Operation.ADD_VALUE));
    }
}