package com.dozz3s.rshidertrash.manager;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BlacklistManager {
    private final JavaPlugin plugin;
    private final Set<String> blacklistedWords;
    private final File blacklistFile;

    public BlacklistManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.blacklistFile = new File(plugin.getDataFolder(), "blacklist_words.yml");
        this.blacklistedWords = new HashSet<>();
        loadBlacklist();
    }

    public void loadBlacklist() {
        try {
            if (!blacklistFile.exists()) {
                plugin.saveResource("blacklist_words.yml", false);
                List<String> defaultWords = Arrays.asList(
                        "анус", "аборт", "бздун", "беспезды", "бздюх", "бля",
                        "блудилище", "блядво", "блядеха", "блядина", "блядистка"
                );
                Files.write(blacklistFile.toPath(), defaultWords, StandardOpenOption.CREATE);
            }

            List<String> lines = Files.readAllLines(blacklistFile.toPath());
            blacklistedWords.clear();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    blacklistedWords.add(line.trim().toLowerCase());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось загрузить blacklist_words.yml: " + e.getMessage());
        }
    }

    public void saveBlacklist() {
        try {
            Files.write(blacklistFile.toPath(), blacklistedWords);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить blacklist_words.yml: " + e.getMessage());
        }
    }

    public Set<String> getBlacklistedWords() {
        return Collections.unmodifiableSet(blacklistedWords);
    }

    public boolean addWord(String word) {
        if (word == null || word.trim().isEmpty()) return false;
        String lowerWord = word.toLowerCase();
        if (blacklistedWords.add(lowerWord)) {
            saveBlacklist();
            return true;
        }
        return false;
    }

    public boolean removeWord(String word) {
        if (word == null || word.trim().isEmpty()) return false;
        String lowerWord = word.toLowerCase();
        if (blacklistedWords.remove(lowerWord)) {
            saveBlacklist();
            return true;
        }
        return false;
    }
}