package com.blockycraft.blockybounty;

import com.blockycraft.blockybounty.command.BountyCommand;
import com.blockycraft.blockybounty.listener.BountyDeathListener;
import com.blockycraft.blockybounty.manager.BountyManager;
import com.blockycraft.blockybounty.database.BountyDatabaseManager;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import org.bukkit.plugin.java.JavaPlugin;

public class BlockyBounty extends JavaPlugin {

    private static BlockyBounty instance;
    private BountyManager bountyManager;
    private BountyDatabaseManager bountyDatabaseManager;
    private MessageManager messageManager;

    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Copiar messages.properties para a pasta do plugin, se não existir
        saveResourceIfNotExists("messages.properties", new File(getDataFolder(), "messages.properties"));

        // Carregar mensagens personalizadas (.properties)
        messageManager = new MessageManager(getDataFolder());

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

    public MessageManager getMessageManager() {
        return messageManager;
    }

    // UTILITÁRIO: Copia um resource do JAR para o disco, caso não exista.
    private void saveResourceIfNotExists(String resource, File targetFile) {
        if (!targetFile.exists()) {
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
                if (in != null) {
                    FileOutputStream out = new FileOutputStream(targetFile);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    in.close();
                    System.out.println("[BlockyBounty] Arquivo " + resource + " copiado para " + targetFile.getAbsolutePath());
                } else {
                    System.err.println("[BlockyBounty] Resource " + resource + " não encontrado no JAR! Crie manualmente.");
                }
            } catch (Exception e) {
                System.err.println("[BlockyBounty] Erro ao copiar " + resource + ": " + e.getMessage());
            }
        }
    }
}
