package com.dozz3s.rshidertrash;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FilterSettings filterSettings;
    private SystemMessageSettings systemSettings;

    private static final Pattern URL_PATTERN = Pattern.compile("(http|https)://[^\\s]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10,}");
    private static final Pattern SPAM_PATTERN = Pattern.compile("[а-яА-Я]{20,}");

    public static class FilterSettings {
        public final boolean phrasesEnabled;
        public final boolean regexEnabled;
        public final Set<String> phrasesDisabledWorlds;
        public final Set<String> regexDisabledWorlds;
        public final Set<String> blockedPhrases;
        public final boolean phrasesMessageEnabled;
        public final String phrasesMessage;
        public final boolean regexMessageEnabled;
        public final String regexMessage;
        public final boolean caseSensitive;

        public FilterSettings(boolean phrasesEnabled, boolean regexEnabled,
                              Set<String> phrasesDisabledWorlds, Set<String> regexDisabledWorlds,
                              Set<String> blockedPhrases, boolean phrasesMessageEnabled,
                              String phrasesMessage, boolean regexMessageEnabled,
                              String regexMessage, boolean caseSensitive) {
            this.phrasesEnabled = phrasesEnabled;
            this.regexEnabled = regexEnabled;
            this.phrasesDisabledWorlds = phrasesDisabledWorlds;
            this.regexDisabledWorlds = regexDisabledWorlds;
            this.blockedPhrases = blockedPhrases;
            this.phrasesMessageEnabled = phrasesMessageEnabled;
            this.phrasesMessage = phrasesMessage;
            this.regexMessageEnabled = regexMessageEnabled;
            this.regexMessage = regexMessage;
            this.caseSensitive = caseSensitive;
        }
    }

    public static class SystemMessageSettings {
        public final BooleanWithWorlds hideJoin;
        public final BooleanWithWorlds hideQuit;
        public final BooleanWithWorlds hideDeath;
        public final BooleanWithWorlds hideKick;

        public SystemMessageSettings(BooleanWithWorlds hideJoin, BooleanWithWorlds hideQuit,
                                     BooleanWithWorlds hideDeath, BooleanWithWorlds hideKick) {
            this.hideJoin = hideJoin;
            this.hideQuit = hideQuit;
            this.hideDeath = hideDeath;
            this.hideKick = hideKick;
        }
    }

    public static class BooleanWithWorlds {
        public final boolean enabled;
        public final Set<String> disabledWorlds;

        public BooleanWithWorlds(boolean enabled, Set<String> disabledWorlds) {
            this.enabled = enabled;
            this.disabledWorlds = disabledWorlds;
        }
    }

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        boolean caseSensitive = config.getBoolean("case-sensitive", false);
        loadFilterSettings(config, caseSensitive);
        loadSystemSettings(config);
    }

    private void loadFilterSettings(FileConfiguration config, boolean caseSensitive) {
        ConfigurationSection phrasesSection = config.getConfigurationSection("block-phrases");
        ConfigurationSection regexSection = config.getConfigurationSection("block-regex");

        Set<String> blockedPhrases = new HashSet<>();
        if (phrasesSection != null) {
            List<String> phrasesList = phrasesSection.getStringList("list");
            blockedPhrases.addAll(phrasesList);

            if (!caseSensitive) {
                Set<String> lowerCasePhrases = new HashSet<>();
                for (String phrase : blockedPhrases) {
                    lowerCasePhrases.add(phrase.toLowerCase());
                }
                blockedPhrases = lowerCasePhrases;
            }
        }

        this.filterSettings = new FilterSettings(
                phrasesSection != null && phrasesSection.getBoolean("enabled", true),
                regexSection != null && regexSection.getBoolean("enabled", true),
                phrasesSection != null ? new HashSet<>(phrasesSection.getStringList("disabled-worlds")) : Collections.<String>emptySet(),
                regexSection != null ? new HashSet<>(regexSection.getStringList("disabled-worlds")) : Collections.<String>emptySet(),
                blockedPhrases,
                phrasesSection != null && phrasesSection.getBoolean("message_to_player_enabled", true),
                ChatColor.translateAlternateColorCodes('&',
                        phrasesSection != null ? phrasesSection.getString("message_to_player", "&cВ вашем сообщении была найдена запрещенная фраза") : ""),
                regexSection != null && regexSection.getBoolean("message_to_player_enabled", true),
                ChatColor.translateAlternateColorCodes('&',
                        regexSection != null ? regexSection.getString("message_to_player", "&cВаше сообщение содержит запрещенный формат текста") : ""),
                caseSensitive
        );
    }

    private void loadSystemSettings(FileConfiguration config) {
        this.systemSettings = new SystemMessageSettings(
                getBooleanWithWorlds(config, "hide-join"),
                getBooleanWithWorlds(config, "hide-quit"),
                getBooleanWithWorlds(config, "hide-death"),
                getBooleanWithWorlds(config, "hide-kick")
        );
    }

    private BooleanWithWorlds getBooleanWithWorlds(FileConfiguration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        return new BooleanWithWorlds(
                section != null && section.getBoolean("enabled", true),
                section != null ? new HashSet<>(section.getStringList("disabled-worlds")) : Collections.<String>emptySet()
        );
    }

    public FilterSettings getFilterSettings() {
        return filterSettings;
    }

    public SystemMessageSettings getSystemSettings() {
        return systemSettings;
    }

    public boolean matchesBlockedRegex(String message) {
        return URL_PATTERN.matcher(message).find() ||
                PHONE_PATTERN.matcher(message).find() ||
                SPAM_PATTERN.matcher(message).find();
    }
}