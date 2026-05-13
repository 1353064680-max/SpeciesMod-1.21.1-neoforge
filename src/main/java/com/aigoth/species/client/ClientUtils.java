package com.aigoth.species.client;

import com.aigoth.species.screen.RaceSelectionScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void openRaceSelectionScreen() {
        try {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().setScreen(new RaceSelectionScreen());
            }
        } catch (Exception e) {
            LOGGER.error("Error opening race selection screen", e);
        }
    }
}