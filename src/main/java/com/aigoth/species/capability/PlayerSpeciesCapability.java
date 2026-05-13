package com.aigoth.species.capability;

import com.aigoth.species.SpeciesMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PlayerSpeciesCapability {
    public static final ResourceLocation PLAYER_SPECIES_NAME = ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "player_species");
    
    // 使用 AttachmentType 替代旧的 Capability 系统（这是 1.20.5+ 推荐的做法）
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SpeciesMod.MOD_ID);
    
    public static final Supplier<AttachmentType<PlayerSpeciesImpl>> PLAYER_SPECIES = ATTACHMENT_TYPES.register(
        "player_species", 
        () -> AttachmentType.builder(holder -> new PlayerSpeciesImpl()).build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}