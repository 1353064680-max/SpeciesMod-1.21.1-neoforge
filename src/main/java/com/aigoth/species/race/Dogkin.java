package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Dogkin extends Race {
    public Dogkin() {
        super("dogkin", Component.translatable("race.species.dogkin"),
              Component.translatable("race.species.dogkin.short_desc"),
              Component.translatable("race.species.dogkin.desc"),
              Component.translatable("race.species.dogkin.benefits"),
              Component.translatable("race.species.dogkin.drawbacks"));
    }

    @Override
    protected void initAttributes() {
        // 增加攻击伤害
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "dogkin_attack_damage_bonus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    }
}