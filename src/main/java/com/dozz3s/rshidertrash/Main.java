package com.dozz3s.rshidertrash;

import com.dozz3s.rshidertrash.manager.BlacklistManager;
import com.dozz3s.rshidertrash.manager.CommandManager;
import com.dozz3s.rshidertrash.manager.ConfigManager;
import com.dozz3s.rshidertrash.manager.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private ConfigManager configManager;
    private BlacklistManager blacklistManager;
    private EventManager eventManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        this.configManager = new ConfigManager(this);
        this.blacklistManager = new BlacklistManager(this);
        this.eventManager = new EventManager(this, configManager, blacklistManager);
        this.commandManager = new CommandManager(this, configManager, blacklistManager);

        getServer().getPluginManager().registerEvents(eventManager, this);
        getCommand("rshidertrash").setExecutor(commandManager);

        getLogger().info("RsHiderTrash успешно запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RsHiderTrash отключен!");
    }
}