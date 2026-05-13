package com.aigoth.species.input;

import com.aigoth.species.SpeciesMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class KeyBindings {
    public static final String KEY_CATEGORY_SPECIES = "key.category.species.species";
    public static final String KEY_TOGGLE_POTION_EFFECTS = "key.species.toggle_potion_effects";
    
    public static final KeyMapping TOGGLE_POTION_EFFECTS = new KeyMapping(
            KEY_TOGGLE_POTION_EFFECTS,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_BACKSLASH, // \ 键
            KEY_CATEGORY_SPECIES
    );
    
    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_POTION_EFFECTS);
    }
}