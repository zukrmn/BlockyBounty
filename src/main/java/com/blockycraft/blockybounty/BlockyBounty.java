package com.blockycraft.blockybounty;

import com.blockycraft.blockybounty.command.BountyCommand;
import com.blockycraft.blockybounty.geoip.GeoIPManager;
import com.blockycraft.blockybounty.lang.LanguageManager;
import com.blockycraft.blockybounty.listener.BountyDeathListener;
import com.blockycraft.blockybounty.manager.BountyManager;
import com.blockycraft.blockybounty.database.BountyDatabaseManager;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.bukkit.plugin.java.JavaPlugin;

public class BlockyBounty extends JavaPlugin {

    private static BlockyBounty instance;
    private BountyManager bountyManager;
    private BountyDatabaseManager bountyDatabaseManager;
    private LanguageManager languageManager;
    private GeoIPManager geoIPManager;
    private Properties properties;
    private File configFile;

    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        reloadProperties();

        languageManager = new LanguageManager(this);
        geoIPManager = new GeoIPManager();

        try {
            File dbFile = new File(getDataFolder(), "bounties.db");
            bountyDatabaseManager = new BountyDatabaseManager(dbFile.getAbsolutePath());
            System.out.println("[BlockyBounty] Banco de dados inicializado.");
        } catch (Exception e) {
            System.err.println("[BlockyBounty] Erro ao iniciar banco de dados!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        bountyManager = new BountyManager(bountyDatabaseManager);

        // Registrar comando principal
        getCommand("bounty").setExecutor(new BountyCommand(this));

        // Registrar listener de mortes
        getServer().getPluginManager().registerEvents(new BountyDeathListener(this), this);

        System.out.println("[BlockyBounty] Plugin ativado!");
    }

    public void onDisable() {
        if (bountyDatabaseManager != null) {
            bountyDatabaseManager.closeConnection();
            System.out.println("[BlockyBounty] Banco de dados fechado.");
        }
        System.out.println("[BlockyBounty] Plugin desativado.");
    }

    public static BlockyBounty getInstance() {
        return instance;
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public BountyDatabaseManager getBountyDatabaseManager() {
        return bountyDatabaseManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public GeoIPManager getGeoIPManager() {
        return geoIPManager;
    }

    public Properties getProperties() {
        if (properties == null) {
            reloadProperties();
        }
        return properties;
    }

    public void reloadProperties() {
        if (configFile == null) {
            configFile = new File(getDataFolder(), "config.properties");
        }
        properties = new Properties();
        try (InputStream input = new java.io.FileInputStream(configFile)) {
            properties.load(input);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(getDataFolder(), "config.properties");
        }
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    if (!getDataFolder().exists()) {
                        getDataFolder().mkdirs();
                    }
                    java.nio.file.Files.copy(in, configFile.toPath());
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
