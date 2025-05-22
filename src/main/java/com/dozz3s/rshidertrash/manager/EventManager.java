package com.dozz3s.rshidertrash.manager;

import com.dozz3s.rshidertrash.Main;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import java.util.regex.*;

public class EventManager implements Listener {
    private final Main plugin;
    private final ConfigManager configManager;
    private final BlacklistManager blacklistManager;

    public EventManager(Main plugin, ConfigManager configManager, BlacklistManager blacklistManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.blacklistManager = blacklistManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        ConfigManager.FilterSettings settings = configManager.getFilterSettings();
        if (settings == null) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        String message = event.getMessage();

        // Проверка запрещенных фраз
        if (settings.phrasesEnabled && !settings.phrasesDisabledWorlds.contains(world.getName())) {
            String censored = censorMessage(message, settings);
            if (!message.equals(censored)) {
                if (settings.phrasesBlockMessage) {
                    event.setCancelled(true);
                } else {
                    event.setMessage(censored);
                }
                if (settings.phrasesMessageEnabled) {
                    player.sendMessage(settings.phrasesMessage);
                }
                return;
            }
        }

        // Проверка regex
        if (settings.regexEnabled && !settings.regexDisabledWorlds.contains(world.getName())) {
            for (Pattern pattern : settings.regexPatterns) {
                if (pattern.matcher(message).find()) {
                    if (settings.regexBlockMessage) {
                        event.setCancelled(true);
                    }
                    if (settings.regexMessageEnabled) {
                        player.sendMessage(settings.regexMessage);
                    }
                    return;
                }
            }
        }
    }

    private String censorMessage(String message, ConfigManager.FilterSettings settings) {
        String lowerMessage = settings.caseSensitive ? message : message.toLowerCase();
        StringBuilder result = new StringBuilder(message);

        for (String word : blacklistManager.getBlacklistedWords()) {
            String searchWord = settings.caseSensitive ? word : word.toLowerCase();
            int wordLen = searchWord.length();
            int index = 0;

            while ((index = lowerMessage.indexOf(searchWord, index)) != -1) {
                // Проверяем границы слова
                if ((index == 0 || !Character.isLetterOrDigit(lowerMessage.charAt(index - 1))) &&
                        (index + wordLen >= lowerMessage.length() ||
                                !Character.isLetterOrDigit(lowerMessage.charAt(index + wordLen)))) {

                    String replacement = createCensoredWord(
                            message.substring(index, index + wordLen),
                            settings.maxSpecialSymbols
                    );
                    result.replace(index, index + wordLen, replacement);
                }
                index += wordLen;
            }
        }
        return result.toString();
    }

    private String createCensoredWord(String word, int maxSymbols) {
        if (word.length() <= 2) {
            return word.charAt(0) + (word.length() > 1 ? "*" : "");
        }

        int stars = word.length() - 2;
        if (stars > maxSymbols) {
            return word.charAt(0) + "*" + word.charAt(word.length() - 1);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(word.charAt(0));
        for (int i = 0; i < stars; i++) sb.append('*');
        sb.append(word.charAt(word.length() - 1));
        return sb.toString();
    }

    private void handleBlock(AsyncPlayerChatEvent event, Player player,
                             boolean sendMessage, String message, boolean block) {
        if (block) event.setCancelled(true);
        if (sendMessage) player.sendMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        handleSystemMessage(event::setJoinMessage, configManager.getSystemSettings().hideJoin, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        handleSystemMessage(event::setQuitMessage, configManager.getSystemSettings().hideQuit, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        handleSystemMessage(event::setDeathMessage, configManager.getSystemSettings().hideDeath, event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        handleSystemMessage(event::setLeaveMessage, configManager.getSystemSettings().hideKick, event.getPlayer());
    }

    private void handleSystemMessage(java.util.function.Consumer<String> setter,
                                     ConfigManager.BooleanWithWorlds setting, Player player) {
        if (setting.enabled && !setting.disabledWorlds.contains(player.getWorld().getName())) {
            setter.accept(null);
        }
    }
}