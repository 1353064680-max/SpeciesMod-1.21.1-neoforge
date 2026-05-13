package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class WingedBeast extends Race {
    public WingedBeast() {
        super("wingedbeast", Component.translatable("race.species.wingedbeast"),
              Component.translatable("race.species.wingedbeast.short_desc"),
              Component.translatable("race.species.wingedbeast.desc"));
    }

    @Override
    protected void initAttributes() {
        // 增加移速
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "wingedbeast_speed_bonus"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
}