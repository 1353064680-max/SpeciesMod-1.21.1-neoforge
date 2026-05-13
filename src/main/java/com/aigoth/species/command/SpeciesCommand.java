package com.aigoth.species.command;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;
import com.aigoth.species.data.PlayerSpeciesData;
import com.aigoth.species.network.OpenRaceSelectionPacket;
import com.aigoth.species.race.Race;
import com.aigoth.species.race.RaceFactory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class SpeciesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("species")
            .requires(source -> source.hasPermission(4)) // 需要权限等级4（管理员或以上）
            .then(Commands.literal("list")
                .executes(SpeciesCommand::listPlayers))
            .then(Commands.literal("player")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(SpeciesCommand::getPlayerRace)))
            .then(Commands.literal("reset")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(SpeciesCommand::resetPlayerRace)))
            .then(Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("race", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .executes(SpeciesCommand::setPlayerRace))))
        );
    }

    private static int listPlayers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        
        source.sendSuccess(() -> Component.literal("=== 在线玩家种族列表 ==="), false);
        
        for (ServerPlayer player : players) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            String raceName = cap.hasSelectedRace() ? 
                cap.getSelectedRace().getRaceName().getString() : 
                "未选择";
            source.sendSuccess(() -> Component.literal(player.getName().getString() + ": " + raceName), false);
        }
        
        return 1;
    }

    private static int getPlayerRace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        
        PlayerSpeciesImpl cap = target.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        if (cap.hasSelectedRace()) {
            Race race = cap.getSelectedRace();
            source.sendSuccess(() -> Component.literal(
                target.getName().getString() + " 的种族是: " + race.getRaceName().getString()), false);
        } else {
            source.sendSuccess(() -> Component.literal(
                target.getName().getString() + " 尚未选择种族"), false);
        }
        
        return 1;
    }

    private static int resetPlayerRace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        
        PlayerSpeciesImpl cap = target.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        // 重置种族选择
        cap.setSelectedRace(null);
        
        // 从数据文件中移除该玩家的种族信息
        PlayerSpeciesData.removePlayerRace(target.getUUID());
        
        // 如果玩家在线，直接发送重新选择种族的数据包
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(target, new OpenRaceSelectionPacket());
        
        source.sendSuccess(() -> Component.literal(
            "已重置玩家 " + target.getName().getString() + " 的种族选择"), false);
        
        return 1;
    }
    
    private static int setPlayerRace(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String raceId = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "race");
        
        // 验证种族ID是否有效
        if (!isValidRaceId(raceId)) {
            source.sendFailure(Component.literal("无效的种族ID: " + raceId));
            return 0;
        }
        
        PlayerSpeciesImpl cap = target.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
        // 创建种族实例
        Race race = RaceFactory.createRaceFromId(raceId);
        if (race != null) {
            cap.setSelectedRace(race);
            
            // 保存到数据文件
            PlayerSpeciesData.setPlayerRace(target.getUUID(), raceId);
            
            source.sendSuccess(() -> Component.literal(
                "已将玩家 " + target.getName().getString() + " 的种族设置为 " + race.getRaceName().getString()), false);
        } else {
            source.sendFailure(Component.literal("无法创建种族实例: " + raceId));
        }
        
        return 1;
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
        
        // 检查种族ID是否在预定义的列表中
        return switch (raceId) {
            case "human", "catkin", "dogkin", "foxkin", "wingedbeast", 
                 "aquatic", "slimekin", "elf", "plantkin", "cursed", "aigoth" -> true;
            default -> false;
        };
    }
}
