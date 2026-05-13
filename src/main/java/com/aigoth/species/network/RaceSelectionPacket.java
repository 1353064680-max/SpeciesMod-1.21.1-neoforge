package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.race.Race;
import com.aigoth.species.race.RaceFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record RaceSelectionPacket(String raceId) implements CustomPacketPayload {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final Type<RaceSelectionPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "race_selection")
    );
    
    public static final StreamCodec<FriendlyByteBuf, RaceSelectionPacket> STREAM_CODEC = 
        new StreamCodec<>() {
            @Override
            public RaceSelectionPacket decode(FriendlyByteBuf buf) {
                return new RaceSelectionPacket(buf.readUtf());
            }

            @Override
            public void encode(FriendlyByteBuf buf, RaceSelectionPacket packet) {
                buf.writeUtf(packet.raceId());
            }
        };
    
    public RaceSelectionPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public static void handle(RaceSelectionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                if (ctx.player() instanceof ServerPlayer player) {
                    // 验证种族ID是否有效，防止恶意数据包
                    if (isValidRaceId(packet.raceId())) {
                        var cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                        try {
                            Race race = RaceFactory.createRaceFromId(packet.raceId());
                            if (race != null) {
                                // 禁止选择彩蛋种族
                                if (!"aigoth".equals(packet.raceId())) {
                                    cap.setSelectedRace(race);
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error creating race from ID: {}", packet.raceId(), e);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error handling RaceSelectionPacket", e);
            }
        });
    }
    
    /**
     * 验证种族ID是否有效
     * @param raceId 种族ID
     * @return 如果有效返回true，否则返回false
     */
    private static boolean isValidRaceId(String raceId) {
        // 检查是否为null或空字符串
        if (raceId == null || raceId.isEmpty()) {
            return false;
        }
        
        // 检查种族ID是否在预定义的列表中（包括彩蛋种族）
        return switch (raceId) {
            case "human", "catkin", "dogkin", "foxkin", "wingedbeast", 
                 "aquatic", "slimekin", "elf", "plantkin", "cursed", "aigoth" -> true;
            default -> false;
        };
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
