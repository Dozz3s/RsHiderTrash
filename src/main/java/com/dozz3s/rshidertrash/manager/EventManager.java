package com.dozz3s.rshidertrash.manager;

import com.dozz3s.rshidertrash.Main;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import java.util.*;
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
        String originalMessage = event.getMessage();
        String censoredMessage = originalMessage;

        if (settings.regexEnabled && !settings.regexDisabledWorlds.contains(world.getName())) {
            censoredMessage = processRegexFilters(censoredMessage, settings);
        }

        if (settings.phrasesEnabled && !settings.phrasesDisabledWorlds.contains(world.getName())) {
            censoredMessage = processBannedWords(censoredMessage, settings);
        }

        if (!originalMessage.equals(censoredMessage)) {
            handleCensoredMessage(event, player, originalMessage, censoredMessage, settings);
        }
    }

    private String processRegexFilters(String message, ConfigManager.FilterSettings settings) {
        for (Map.Entry<String, Pattern> entry : settings.regexFilters.entrySet()) {
            message = applyRegexCensoring(message, entry.getValue(), settings.maxSpecialSymbols);
        }
        return message;
    }

    private String applyRegexCensoring(String message, Pattern pattern, int maxSymbols) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String found = matcher.group();
            matcher.appendReplacement(result, censorWithLimit(found, maxSymbols));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String processBannedWords(String message, ConfigManager.FilterSettings settings) {
        String lowerMessage = settings.caseSensitive ? message : message.toLowerCase();
        StringBuilder result = new StringBuilder(message);

        for (String word : blacklistManager.getBlacklistedWords()) {
            String searchWord = settings.caseSensitive ? word : word.toLowerCase();
            int wordLen = searchWord.length();
            int index = 0;

            while ((index = lowerMessage.indexOf(searchWord, index)) != -1) {
                if (isWholeWord(lowerMessage, index, wordLen)) {
                    String replacement = censorWithLimit(
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

    private String censorWithLimit(String word, int maxSymbols) {
        if (word.length() <= 1) return "*";

        char first = word.charAt(0);
        char last = word.charAt(word.length() - 1);

        int availableStars = Math.min(word.length() - 2, maxSymbols);

        if (availableStars < word.length() - 2) {
            return first + "*" + last;
        }

        return first + createStars(availableStars) + last;
    }

    private String createStars(int count) {
        if (count <= 0) return "";
        char[] stars = new char[count];
        Arrays.fill(stars, '*');
        return new String(stars);
    }

    private boolean isWholeWord(String text, int index, int length) {
        boolean startBoundary = index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1));
        boolean endBoundary = (index + length >= text.length()) ||
                !Character.isLetterOrDigit(text.charAt(index + length));
        return startBoundary && endBoundary;
    }

    private void handleCensoredMessage(AsyncPlayerChatEvent event, Player player,
                                       String original, String censored,
                                       ConfigManager.FilterSettings settings) {
        boolean isRegexViolation = checkRegexViolation(original, censored, settings);

        boolean shouldBlock = isRegexViolation ? settings.regexBlockMessage : settings.phrasesBlockMessage;
        boolean shouldNotify = isRegexViolation ? settings.regexMessageEnabled : settings.phrasesMessageEnabled;
        String message = isRegexViolation ? settings.regexMessage : settings.phrasesMessage;

        if (shouldBlock) {
            event.setCancelled(true);
        } else {
            event.setMessage(censored);
        }

        if (shouldNotify) {
            player.sendMessage(message);
        }
    }

    private boolean checkRegexViolation(String original, String censored, ConfigManager.FilterSettings settings) {
        if (!settings.regexEnabled) return false;

        ConfigManager.FilterSettings tempSettings = new ConfigManager.FilterSettings(
                false, settings.regexEnabled,
                settings.phrasesDisabledWorlds, settings.regexDisabledWorlds,
                settings.phrasesBlockMessage, settings.phrasesMessageEnabled,
                settings.phrasesMessage, settings.regexBlockMessage,
                settings.regexMessageEnabled, settings.regexMessage,
                settings.caseSensitive, settings.maxSpecialSymbols,
                settings.regexFilters
        );

        String regexOnlyCensored = processRegexFilters(original, tempSettings);
        return !original.equals(regexOnlyCensored);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        handleSystemMessage(event::setJoinMessage,
                configManager.getSystemSettings().hideJoin,
                event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        handleSystemMessage(event::setQuitMessage,
                configManager.getSystemSettings().hideQuit,
                event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        handleSystemMessage(event::setDeathMessage,
                configManager.getSystemSettings().hideDeath,
                event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        handleSystemMessage(event::setLeaveMessage,
                configManager.getSystemSettings().hideKick,
                event.getPlayer());
    }

    private void handleSystemMessage(java.util.function.Consumer<String> setter,
                                     ConfigManager.BooleanWithWorlds setting,
                                     Player player) {
        if (setting.enabled && !setting.disabledWorlds.contains(player.getWorld().getName())) {
            setter.accept(null);
        }
    }
}