package com.dozz3s.rshidertrash.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FilterSettings filterSettings;
    private SystemMessageSettings systemSettings;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        boolean caseSensitive = config.getBoolean("case-sensitive", false);
        int maxSpecialSymbols = config.getInt("max_special_symbols", 5);
        loadFilterSettings(config, caseSensitive, maxSpecialSymbols);
        loadSystemSettings(config);
    }

    private void loadFilterSettings(FileConfiguration config, boolean caseSensitive, int maxSpecialSymbols) {
        ConfigurationSection phrasesSection = config.getConfigurationSection("block-phrases");
        ConfigurationSection regexSection = config.getConfigurationSection("block-regex");

        Set<Pattern> regexPatterns = new HashSet<>();
        if (regexSection != null && regexSection.getBoolean("enabled", false)) {
            for (String regex : regexSection.getStringList("list")) {
                try {
                    int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
                    regexPatterns.add(Pattern.compile(regex, flags));
                } catch (Exception e) {
                    plugin.getLogger().warning("Некорректное regex выражение: " + regex);
                }
            }
        }

        this.filterSettings = new FilterSettings(
                phrasesSection != null && phrasesSection.getBoolean("enabled", true),
                regexSection != null && regexSection.getBoolean("enabled", false),
                phrasesSection != null ? new HashSet<>(phrasesSection.getStringList("disabled-worlds")) : Collections.emptySet(),
                regexSection != null ? new HashSet<>(regexSection.getStringList("disabled-worlds")) : Collections.emptySet(),
                phrasesSection != null && phrasesSection.getBoolean("block_message", true),
                phrasesSection != null && phrasesSection.getBoolean("message_to_player_enabled", true),
                ChatColor.translateAlternateColorCodes('&',
                        phrasesSection != null ? phrasesSection.getString("message_to_player", "&cВ вашем сообщении была найдена запрещенная фраза") : ""),
                regexSection != null && regexSection.getBoolean("block_message", true),
                regexSection != null && regexSection.getBoolean("message_to_player_enabled", true),
                ChatColor.translateAlternateColorCodes('&',
                        regexSection != null ? regexSection.getString("message_to_player", "&cВаше сообщение содержит запрещенный формат текста") : ""),
                caseSensitive,
                maxSpecialSymbols,
                regexPatterns
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
                section != null ? new HashSet<>(section.getStringList("disabled-worlds")) : Collections.emptySet()
        );
    }

    public FilterSettings getFilterSettings() {
        return filterSettings;
    }

    public SystemMessageSettings getSystemSettings() {
        return systemSettings;
    }

    public static class FilterSettings {
        public final boolean phrasesEnabled;
        public final boolean regexEnabled;
        public final Set<String> phrasesDisabledWorlds;
        public final Set<String> regexDisabledWorlds;
        public final boolean phrasesBlockMessage;
        public final boolean phrasesMessageEnabled;
        public final String phrasesMessage;
        public final boolean regexBlockMessage;
        public final boolean regexMessageEnabled;
        public final String regexMessage;
        public final boolean caseSensitive;
        public final int maxSpecialSymbols;
        public final Set<Pattern> regexPatterns;

        public FilterSettings(boolean phrasesEnabled, boolean regexEnabled,
                              Set<String> phrasesDisabledWorlds, Set<String> regexDisabledWorlds,
                              boolean phrasesBlockMessage, boolean phrasesMessageEnabled,
                              String phrasesMessage, boolean regexBlockMessage,
                              boolean regexMessageEnabled, String regexMessage,
                              boolean caseSensitive, int maxSpecialSymbols,
                              Set<Pattern> regexPatterns) {
            this.phrasesEnabled = phrasesEnabled;
            this.regexEnabled = regexEnabled;
            this.phrasesDisabledWorlds = phrasesDisabledWorlds;
            this.regexDisabledWorlds = regexDisabledWorlds;
            this.phrasesBlockMessage = phrasesBlockMessage;
            this.phrasesMessageEnabled = phrasesMessageEnabled;
            this.phrasesMessage = phrasesMessage;
            this.regexBlockMessage = regexBlockMessage;
            this.regexMessageEnabled = regexMessageEnabled;
            this.regexMessage = regexMessage;
            this.caseSensitive = caseSensitive;
            this.maxSpecialSymbols = maxSpecialSymbols;
            this.regexPatterns = Collections.unmodifiableSet(regexPatterns);
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
}