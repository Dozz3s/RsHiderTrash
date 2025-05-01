package com.dozz3s.rshidertrash;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public CommandManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rshidertrash.reload")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды!");
                return true;
            }

            configManager.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация RsHiderTrash успешно перезагружена!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Использование: /rshidertrash reload");
        return true;
    }
}