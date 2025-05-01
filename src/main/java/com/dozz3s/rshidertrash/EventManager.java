package com.dozz3s.rshidertrash;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class EventManager implements Listener {
    private final RsHiderTrash plugin;
    private final ConfigManager configManager;

    public EventManager(RsHiderTrash plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        ConfigManager.FilterSettings filterSettings = configManager.getFilterSettings();
        if (filterSettings == null) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        String message = event.getMessage();

        if (filterSettings.phrasesEnabled && !filterSettings.phrasesDisabledWorlds.contains(world.getName())) {
            String checkMessage = filterSettings.caseSensitive ? message : message.toLowerCase();
            for (String phrase : filterSettings.blockedPhrases) {
                if (checkMessage.contains(phrase)) {
                    event.setCancelled(true);
                    if (filterSettings.phrasesMessageEnabled) {
                        player.sendMessage(filterSettings.phrasesMessage);
                    }
                    return;
                }
            }
        }

        if (filterSettings.regexEnabled && !filterSettings.regexDisabledWorlds.contains(world.getName())) {
            if (configManager.matchesBlockedRegex(message)) {
                event.setCancelled(true);
                if (filterSettings.regexMessageEnabled) {
                    player.sendMessage(filterSettings.regexMessage);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        ConfigManager.SystemMessageSettings settings = configManager.getSystemSettings();
        if (settings != null && settings.hideJoin.enabled &&
                !settings.hideJoin.disabledWorlds.contains(event.getPlayer().getWorld().getName())) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        ConfigManager.SystemMessageSettings settings = configManager.getSystemSettings();
        if (settings != null && settings.hideQuit.enabled &&
                !settings.hideQuit.disabledWorlds.contains(event.getPlayer().getWorld().getName())) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        ConfigManager.SystemMessageSettings settings = configManager.getSystemSettings();
        if (settings != null && settings.hideDeath.enabled &&
                !settings.hideDeath.disabledWorlds.contains(event.getEntity().getWorld().getName())) {
            event.setDeathMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        ConfigManager.SystemMessageSettings settings = configManager.getSystemSettings();
        if (settings != null && settings.hideKick.enabled &&
                !settings.hideKick.disabledWorlds.contains(event.getPlayer().getWorld().getName())) {
            event.setLeaveMessage("");
        }
    }
}