package com.xai.ultimatelibrary;

import org.bukkit.plugin.java.JavaPlugin;

public class UltimateLibraryPlugin extends JavaPlugin {

    private static UltimateLibraryPlugin instance;
    private StructureBuilder structureBuilder;
    private AltarManager altarManager;
    private BossManager bossManager;
    private WeaponManager weaponManager;

    @Override
    public void onEnable() {
        instance = this;
        structureBuilder = new StructureBuilder(this);
        altarManager = new AltarManager(this);
        bossManager = new BossManager(this);
        weaponManager = new WeaponManager(this);

        getLogger().info("§6[UltimateLibrary] §aComplete Library Dungeon plugin loaded successfully!");

        // Register commands
        getCommand("buildlibrary").setExecutor(new CommandHandler(this));
        getCommand("regeneratelibrary").setExecutor(new CommandHandler(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(altarManager, this);
        getServer().getPluginManager().registerEvents(bossManager, this);
        getServer().getPluginManager().registerEvents(weaponManager, this);
    }

    public static UltimateLibraryPlugin getInstance() {
        return instance;
    }

    public StructureBuilder getStructureBuilder() {
        return structureBuilder;
    }

    public AltarManager getAltarManager() {
        return altarManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
}
