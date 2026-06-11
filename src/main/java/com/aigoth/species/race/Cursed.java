package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Cursed extends Race {
    public Cursed() {
        super("cursed", Component.translatable("race.species.cursed"),
              Component.translatable("race.species.cursed.short_desc"),
              Component.translatable("race.species.cursed.benefits"),
              Component.translatable("race.species.cursed.drawbacks"),
              Component.literal(""),
              Component.literal(""));
    }

    @Override
    protected void initAttributes() {
        // 增加护甲值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.armor"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "cursed_armor_bonus"), 4.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加攻击伤害
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "cursed_attack_damage_bonus"), 3.0, AttributeModifier.Operation.ADD_VALUE));
    }
}