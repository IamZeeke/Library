package com.xai.ultimatelibrary;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final UltimateLibraryPlugin plugin;

    public CommandHandler(UltimateLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cPlayers only!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cAdmin only!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("buildlibrary")) {
            player.sendMessage("§6[Library] §eBuilding full dungeon...");
            plugin.getStructureBuilder().buildFullDungeon(player);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("regeneratelibrary")) {
            player.sendMessage("§6[Library] §eRegenerating dungeon...");
            plugin.getStructureBuilder().regenerateDungeon(player);
            return true;
        }

        return true;
    }
}
