package com.aigoth.species.item;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.network.OpenRaceSelectionPacket;
import com.aigoth.species.race.Race;
import com.aigoth.species.race.RaceFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class RaceStoneItem extends Item {
    private final String raceId;
    private final boolean isResetStone; // 是否为重置石（灵魂之石）

    public RaceStoneItem(Properties properties, String raceId) {
        super(properties);
        this.raceId = raceId;
        this.isResetStone = "reset".equals(raceId);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);
        
        if (isResetStone) {
            // 灵魂之石逻辑 - 重置种族选择
            resetPlayerRace(player);
        } else {
            // 种族之石逻辑 - 设置特定种族
            setPlayerRace(player, raceId);
        }
        
        // 消耗物品（除非是创造模式）
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        return InteractionResultHolder.success(stack);
    }

    private void resetPlayerRace(Player player) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        // 重置种族选择
        cap.setSelectedRace(null);
        
        // 如果玩家在线，直接发送重新选择种族的数据包
        if (player instanceof ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new OpenRaceSelectionPacket());
        }
    }

    private void setPlayerRace(Player player, String raceId) {
        PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        // 创建种族实例
        Race race = RaceFactory.createRaceFromId(raceId);
        if (race != null) {
            cap.setSelectedRace(race);
            player.sendSystemMessage(Component.translatable("item.species.race_stone.set_race", race.getRaceName()));
        } else {
            player.sendSystemMessage(Component.translatable("item.species.race_stone.invalid_race").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        if (isResetStone) {
            // 灵魂之石的提示
            tooltip.add(Component.translatable("item.species.soul_stone.tooltip").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.species.soul_stone.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            // 种族之石的提示
            Race race = RaceFactory.createRaceFromId(raceId);
            if (race != null) {
                MutableComponent raceName = race.getRaceName().copy();
                tooltip.add(Component.translatable("item.species.race_stone.tooltip", raceName).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}