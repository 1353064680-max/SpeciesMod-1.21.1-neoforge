package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Elf extends Race {
    public Elf() {
        super("elf", Component.translatable("race.species.elf"), 
              Component.translatable("race.species.elf.short_desc"),
              Component.translatable("race.species.elf.desc"),
              Component.translatable("race.species.elf.benefits"),
              Component.translatable("race.species.elf.drawbacks"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值（减少2颗心，即4.0点生命值）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_health_penalty"), -4.0, AttributeModifier.Operation.ADD_VALUE));
        
        // 近战伤害减少2.0点（与远程伤害增强平衡）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.attack_damage"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_melee_damage_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));
        
        // 减少护甲值2点
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.armor"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "elf_armor_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));
    }
}