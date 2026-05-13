package com.aigoth.species.capability;

import com.aigoth.species.race.Race;
import net.minecraft.nbt.CompoundTag;

public interface PlayerSpecies {
    Race getSelectedRace();
    void setSelectedRace(Race race);
    boolean hasSelectedRace();
    
    // 添加种族效果开关相关方法
    boolean isRacialEffectsEnabled();
    void setRacialEffectsEnabled(boolean enabled);
    
    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);
}