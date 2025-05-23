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

        Map<String, Pattern> regexFilters = new LinkedHashMap<>();
        if (regexSection != null && regexSection.getBoolean("enabled", false)) {
            ConfigurationSection filtersSection = regexSection.getConfigurationSection("filters");
            if (filtersSection != null) {
                loadRegexFilter(filtersSection, "urls",
                        "(http|https)://[^\\s]+", caseSensitive, regexFilters);
                loadRegexFilter(filtersSection, "phones",
                        "\\d{10,}", caseSensitive, regexFilters);
                loadRegexFilter(filtersSection, "spam-text",
                        "[а-яА-Я]{20,}", caseSensitive, regexFilters);
                loadRegexFilter(filtersSection, "domains",
                        "\\b(?:[a-z0-9]+(?:\\-[a-z0-9]+)*\\.)+[a-z]{2,}\\b", caseSensitive, regexFilters);
                loadRegexFilter(filtersSection, "obscene",
                        "(?iu)\\b(?:(?:(?:у|[нз]а|(?:хитро|не)?вз?[ыьъ]|с[ьъ]|(?:и|ра)[зс]ъ?|(?:о[тб]|п[оа]д)[ьъ]?|(?:.\\B)+?[оаеи-])-?)?(?:[её](?:б(?!о[рй]|рач)|п[уа](?:ц|тс))|и[пб][ае][тцд][ьъ]).*?|(?:(?:н[иеа]|(?:ра|и)[зс]|[зд]?[ао](?:т|дн[оа])?|с(?:м[еи])?|а[пб]ч|в[ъы]?|пр[еи])-?)?ху(?:[яйиеёю]|л+и(?!ган)).*?|бл(?:[эя]|еа?)(?:[дт][ьъ]?)?|\\S*?(?:п(?:[иеё]зд|ид[аое]?р|ед(?:р(?!о)|[аое]р|ик)|охую)|бля(?:[дбц]|тс)|[ое]ху[яйиеё]|хуйн).*?|(?:о[тб]?|про|на|вы)?м(?:анд(?:[ауеыи](?:л(?:и[сзщ])?[ауеиы])?|ой|[ао]в.*?|юк(?:ов|[ауи])?|е[нт]ь|ища)|уд(?:[яаиое].+?|е?н(?:[ьюия]|ей))|[ао]л[ао]ф[ьъ](?:[яиюе]|[еёо]й))|елд[ауые].*?|ля[тд]ь|(?:[нз]а|по)х)\\b",
                        caseSensitive, regexFilters);
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
                regexFilters
        );
    }

    private void loadRegexFilter(ConfigurationSection filtersSection, String filterKey,
                                 String defaultPattern, boolean caseSensitive,
                                 Map<String, Pattern> regexFilters) {
        ConfigurationSection filterSection = filtersSection.getConfigurationSection(filterKey);
        boolean enabled = filterSection == null || filterSection.getBoolean("enabled", true);

        if (enabled) {
            try {
                String pattern = filterSection != null ?
                        filterSection.getString("pattern", defaultPattern) : defaultPattern;
                int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
                regexFilters.put(filterKey, Pattern.compile(pattern, flags));
            } catch (Exception e) {
                plugin.getLogger().warning("Некорректное regex выражение для фильтра " + filterKey + ": " + e.getMessage());
            }
        }
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
        public final Map<String, Pattern> regexFilters;

        public FilterSettings(boolean phrasesEnabled, boolean regexEnabled,
                              Set<String> phrasesDisabledWorlds, Set<String> regexDisabledWorlds,
                              boolean phrasesBlockMessage, boolean phrasesMessageEnabled,
                              String phrasesMessage, boolean regexBlockMessage,
                              boolean regexMessageEnabled, String regexMessage,
                              boolean caseSensitive, int maxSpecialSymbols,
                              Map<String, Pattern> regexFilters) {
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
            this.regexFilters = Collections.unmodifiableMap(regexFilters);
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