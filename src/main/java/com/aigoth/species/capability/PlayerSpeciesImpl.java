package com.aigoth.species.capability;

import com.aigoth.species.Config;
import com.aigoth.species.data.PlayerSpeciesData;
import com.aigoth.species.race.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PlayerSpeciesImpl implements PlayerSpecies {
    private Race selectedRace = null;
    private Player playerRef = null; // 保存玩家引用用于数据存储
    private boolean racialEffectsEnabled = true; // 种族效果开关状态，默认开启

    public void setPlayerReference(Player player) {
        this.playerRef = player;
        // 当设置玩家引用时，如果已经有种族，则根据开关状态决定是否应用种族效果
        if (playerRef != null && selectedRace != null && racialEffectsEnabled) {
            selectedRace.applyEffects(playerRef);
        }
    }

    @Override
    public Race getSelectedRace() {
        return selectedRace;
    }

    @Override
    public void setSelectedRace(Race race) {
        // 如果之前有种族，移除之前种族的效果
        if (this.selectedRace != null && this.playerRef != null) {
            this.selectedRace.removeEffects(this.playerRef);
        }
        
        this.selectedRace = race;
        
        // 如果有玩家引用且种族效果开启，应用新种族的效果
        if (playerRef != null && selectedRace != null && racialEffectsEnabled) {
            selectedRace.applyEffects(playerRef);
        }
        
        // 如果有玩家引用，保存数据到文件
        if (playerRef != null && playerRef instanceof net.minecraft.server.level.ServerPlayer) {
            if (race != null) {
                PlayerSpeciesData.setPlayerRace(playerRef.getUUID(), race.getRaceId());
            } else {
                PlayerSpeciesData.removePlayerRace(playerRef.getUUID());
            }
        }
        // 注意：如果没有玩家引用，无法保存数据，因为缺少有效的UUID
        // 在专用服务器环境中，玩家数据应该通过玩家对象来保存
    }

    @Override
    public boolean hasSelectedRace() {
        return selectedRace != null;
    }
    
    @Override
    public boolean isRacialEffectsEnabled() {
        return racialEffectsEnabled;
    }
    
    @Override
    public void setRacialEffectsEnabled(boolean enabled) {
        boolean oldState = this.racialEffectsEnabled;
        this.racialEffectsEnabled = enabled;
        // 如果有玩家引用和种族
        if (playerRef != null && selectedRace != null) {
            if (enabled && !oldState) {
                // 开启效果
                selectedRace.applyEffects(playerRef);
            } else if (!enabled && oldState) {
                // 关闭效果
                selectedRace.removeEffects(playerRef);
            }
        }
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (selectedRace != null) {
            tag.putString("raceId", selectedRace.getRaceId());
        }
        tag.putBoolean("racialEffectsEnabled", racialEffectsEnabled);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("raceId")) {
            String raceId = nbt.getString("raceId");
            // 根据raceId创建对应的种族实例
            selectedRace = RaceFactory.createRaceFromId(raceId);
        }
        if (nbt.contains("racialEffectsEnabled")) {
            racialEffectsEnabled = nbt.getBoolean("racialEffectsEnabled");
        } else {
            // 如果配置中设置了默认值，则使用配置的默认值
            racialEffectsEnabled = Config.enableRacialPotionEffectsByDefault.get();
        }
    }
}