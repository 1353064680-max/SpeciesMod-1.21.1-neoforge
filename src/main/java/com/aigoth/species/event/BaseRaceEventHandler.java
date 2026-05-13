package com.aigoth.species.event;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.data.PlayerSpeciesData;
import com.aigoth.species.network.OpenRaceSelectionPacket;
import com.aigoth.species.race.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRaceEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseRaceEventHandler.class);
    
    // 服务器启动时初始化数据文件
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        PlayerSpeciesData.setDataPath(event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
    }

    // 玩家登录时检查是否选择种族
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (!cap.hasSelectedRace()) {
                String savedRaceId = PlayerSpeciesData.getPlayerRace(player.getUUID());
                if (savedRaceId != null) {
                    Race race = RaceFactory.createRaceFromId(savedRaceId);
                    if (race != null) {
                        cap.setSelectedRace(race);
                        race.applyEffects(player);
                    } else {
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenRaceSelectionPacket());
                    }
                } else {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new OpenRaceSelectionPacket());
                }
            } else {
                cap.getSelectedRace().applyEffects(player);
            }
        }
    }

    // 玩家登出时保存种族数据
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace()) {
                PlayerSpeciesData.setPlayerRace(player.getUUID(), cap.getSelectedRace().getRaceId());
            }
        }
    }

    // 玩家克隆时（包括死亡后复活）复制种族数据
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSpeciesImpl oldCap = event.getOriginal().getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            PlayerSpeciesImpl newCap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            
            if (oldCap.hasSelectedRace()) {
                Race race = RaceFactory.createRaceFromId(oldCap.getSelectedRace().getRaceId());
                if (race != null) {
                    newCap.setSelectedRace(race);
                    race.applyEffects(player);
                    if (newCap.isRacialEffectsEnabled()) {
                        RaceEventHandler.applyRacialEffects(player, race, true);
                    }
                }
            }
        }
    }

    // 玩家即将死亡时保存种族数据
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace()) {
                String raceId = cap.getSelectedRace().getRaceId();
                PlayerSpeciesData.setPlayerRace(player.getUUID(), raceId);
                LOGGER.info("玩家 {} 死亡前保存种族数据: {}", player.getName().getString(), raceId);
            }
        }
    }
    
    // 玩家复活时重新应用种族效果
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PlayerSpeciesImpl cap = event.getEntity().getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (cap.hasSelectedRace()) {
            cap.getSelectedRace().applyEffects(event.getEntity());
            LOGGER.info("玩家 {} 复活后恢复种族: {}", event.getEntity().getName().getString(), cap.getSelectedRace().getRaceId());
        } else {
            String savedRaceId = PlayerSpeciesData.getPlayerRace(event.getEntity().getUUID());
            if (savedRaceId != null) {
                Race race = RaceFactory.createRaceFromId(savedRaceId);
                if (race != null) {
                    cap.setSelectedRace(race);
                    race.applyEffects(event.getEntity());
                    LOGGER.info("玩家 {} 从文件系统恢复种族数据: {}", event.getEntity().getName().getString(), savedRaceId);
                }
            } else {
                LOGGER.warn("玩家 {} 复活时未找到种族数据", event.getEntity().getName().getString());
            }
        }
    }

    // 玩家更新时应用种族效果
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 20 == 0) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace()) {
                Race race = cap.getSelectedRace();
                race.applyEffects(player);
                RaceEventHandler.applyRacialEffects(player, race, cap.isRacialEffectsEnabled());
            }
        }
    }

    // 玩家维度改变时重新应用种族效果
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PlayerSpeciesImpl cap = event.getEntity().getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (cap.hasSelectedRace()) {
            cap.getSelectedRace().applyEffects(event.getEntity());
            RaceEventHandler.applyRacialEffects(event.getEntity(), cap.getSelectedRace(), cap.isRacialEffectsEnabled());
        }
    }
}