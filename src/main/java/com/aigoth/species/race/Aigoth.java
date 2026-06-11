package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class Aigoth extends Race {
    public Aigoth() {
        super("aigoth", Component.translatable("race.species.aigoth"), 
              Component.translatable("race.species.aigoth.short_desc"),
              Component.translatable("race.species.aigoth.desc"));
    }

    @Override
    protected void initAttributes() {
        // 水生族属性：增加游泳速度
        attributeModifiers.put(ResourceLocation.fromNamespaceAndPath("neoforge", "swim_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_swimming_speed"), 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        // 水生族属性：增加幸运
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.luck"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_luck_bonus"), 0.5, AttributeModifier.Operation.ADD_VALUE));
        
        // 增加摔落缓冲距离（8格以下免疫）
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_fall_resistance_bonus"), 8.0, AttributeModifier.Operation.ADD_VALUE));

        //水下挖掘速度增加
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("player.submerged_mining_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "aigoth_mining_speed_bonus"), 1.3, AttributeModifier.Operation.ADD_VALUE));
    }
}