package com.aigoth.species.network;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenRaceSelectionPacket() implements CustomPacketPayload {
    public static final Type<OpenRaceSelectionPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "open_race_selection")
    );
    
    public static final StreamCodec<FriendlyByteBuf, OpenRaceSelectionPacket> STREAM_CODEC = 
        StreamCodec.unit(new OpenRaceSelectionPacket());

    public OpenRaceSelectionPacket(FriendlyByteBuf buf) {
        this();
    }

    public static void handle(OpenRaceSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                // 在客户端打开种族选择界面
                if (net.neoforged.fml.loading.FMLLoader.getDist().isClient()) {
                    com.aigoth.species.client.ClientUtils.openRaceSelectionScreen();
                }
            } catch (Exception e) {
                SpeciesMod.log.error("Error handling OpenRaceSelectionPacket", e);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
