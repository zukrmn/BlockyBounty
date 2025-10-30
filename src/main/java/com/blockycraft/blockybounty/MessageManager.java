package com.blockycraft.blockybounty;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.bukkit.ChatColor;

public class MessageManager {
    private Properties props = new Properties();

    public MessageManager(File dataFolder) {
        try {
            File msgFile = new File(dataFolder, "messages.properties");
            if (msgFile.exists())
                props.load(new FileInputStream(msgFile));
        } catch (Exception e) {
            System.err.println("[BlockyBounty] Erro ao carregar messages.properties: " + e.getMessage());
        }
    }

    public String get(String key, String fallback) {
        String raw = props.getProperty(key, fallback);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
