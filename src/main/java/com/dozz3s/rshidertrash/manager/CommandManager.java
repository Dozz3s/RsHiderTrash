package com.dozz3s.rshidertrash.manager;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final BlacklistManager blacklistManager;

    public CommandManager(JavaPlugin plugin, ConfigManager configManager, BlacklistManager blacklistManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.blacklistManager = blacklistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "addword":
                return handleAddWord(sender, args);
            case "removeword":
                return handleRemoveWord(sender, args);
            case "listwords":
                return handleListWords(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("rshidertrash.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        configManager.reloadConfig();
        blacklistManager.loadBlacklist();
        sender.sendMessage(ChatColor.GREEN + "Конфигурация и черный список перезагружены!");
        return true;
    }

    private boolean handleAddWord(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rshidertrash.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /rshidertrash addword <слово>");
            return true;
        }

        String word = args[1];
        if (blacklistManager.addWord(word)) {
            sender.sendMessage(ChatColor.GREEN + "Слово '" + word + "' добавлено в черный список!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Слово '" + word + "' уже есть в черном списке!");
        }
        return true;
    }

    private boolean handleRemoveWord(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rshidertrash.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Использование: /rshidertrash removeword <слово>");
            return true;
        }

        String word = args[1];
        if (blacklistManager.removeWord(word)) {
            sender.sendMessage(ChatColor.GREEN + "Слово '" + word + "' удалено из черного списка!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Слова '" + word + "' нет в черном списке!");
        }
        return true;
    }

    private boolean handleListWords(CommandSender sender) {
        if (!sender.hasPermission("rshidertrash.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав на эту команду!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Запрещенные слова (" + blacklistManager.getBlacklistedWords().size() + "):");
        for (String word : blacklistManager.getBlacklistedWords()) {
            sender.sendMessage(ChatColor.YELLOW + "- " + word);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "RsHiderTrash команды:");
        if (sender.hasPermission("rshidertrash.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/rshidertrash reload - Перезагрузить конфиг");
        }
        if (sender.hasPermission("rshidertrash.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/rshidertrash addword <слово> - Добавить слово");
            sender.sendMessage(ChatColor.YELLOW + "/rshidertrash removeword <слово> - Удалить слово");
            sender.sendMessage(ChatColor.YELLOW + "/rshidertrash listwords - Список слов");
        }
    }
}