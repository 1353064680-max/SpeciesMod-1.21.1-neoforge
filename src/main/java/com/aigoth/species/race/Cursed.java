package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Cursed extends Race {
    public Cursed() {
        super("cursed", Component.translatable("race.species.cursed"),
              Component.translatable("race.species.cursed.short_desc"),
              createDescription());
    }

    private static MutableComponent createDescription() {
        // 创建警告文本（红色加粗）
        MutableComponent warning = Component.translatable("race.species.cursed.warning");
        
        // 创建特性描述文本
        MutableComponent description = Component.translatable("race.species.cursed.desc");
        
        // 组合文本
        return Component.empty().append(description).append("\n").append(warning);
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