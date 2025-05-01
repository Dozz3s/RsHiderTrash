package com.dozz3s.rshidertrash;

import org.bukkit.plugin.java.JavaPlugin;

public final class RsHiderTrash extends JavaPlugin {
    private ConfigManager configManager;
    private EventManager eventManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.eventManager = new EventManager(this, configManager);
        this.commandManager = new CommandManager(this, configManager);

        getServer().getPluginManager().registerEvents(eventManager, this);
        getCommand("rshidertrash").setExecutor(commandManager);

        getLogger().info("RsHiderTrash enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RsHiderTrash disabled!");
    }
}