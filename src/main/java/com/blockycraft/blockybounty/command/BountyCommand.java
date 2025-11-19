package com.blockycraft.blockybounty.command;

import com.blockycraft.blockybounty.BlockyBounty;
import com.blockycraft.blockybounty.lang.LanguageManager;
import com.blockycraft.blockybounty.geoip.GeoIPManager;
import com.blockycraft.blockybounty.manager.BountyManager;
import com.blockycraft.blockybounty.data.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BountyCommand implements CommandExecutor {

    private final BountyManager bountyManager;
    private final LanguageManager languageManager;
    private final GeoIPManager geoIPManager;

    public BountyCommand(BlockyBounty plugin) {
        this.bountyManager = plugin.getBountyManager();
        this.languageManager = plugin.getLanguageManager();
        this.geoIPManager = plugin.getGeoIPManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("en", "bounty.only-players"));
            return true;
        }
        Player player = (Player) sender;
        String lang = geoIPManager.getPlayerLanguage(player);

        if (args.length == 0) {
            sendUsage(player, lang);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("remover") && args.length == 2) {
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage(languageManager.get(lang, "bounty.target-offline"));
                return true;
            }
            List<Bounty> bounties = bountyManager.getRemovableBountiesForSetter(player.getUniqueId().toString(), target.getUniqueId().toString());
            if (bounties.isEmpty()) {
                player.sendMessage(languageManager.get(lang, "bounty.no-bounty"));
                return true;
            }
            int total = 0;
            for (Bounty bounty : bounties) {
                total += bounty.getAmount();
                bountyManager.removeBounty(player.getUniqueId().toString(), target.getUniqueId().toString());
            }
            player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, total));
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("target", target.getName());
            placeholders.put("amount", String.valueOf(total));
            player.sendMessage(languageManager.get(lang, "bounty.bounty-removed", placeholders));

            Map<String, String> broadcastPlaceholders = new HashMap<>();
            broadcastPlaceholders.put("setter", player.getName());
            broadcastPlaceholders.put("target", target.getName());
            Bukkit.broadcastMessage(languageManager.get("en", "bounty.remove-broadcast", broadcastPlaceholders));
            return true;
        }

        if (args.length == 2) {
            String targetName = args[0];
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(languageManager.get(lang, "bounty.invalid-amount"));
                return true;
            }
            if (amount < 1) {
                player.sendMessage(languageManager.get(lang, "bounty.amount-positive"));
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage(languageManager.get(lang, "bounty.target-offline"));
                return true;
            }
            boolean bloqueia = false;
            try {
                Class<?> api = Class.forName("com.blockycraft.blockygroups.api.BlockyGroupsAPI");
                String setterGroup = (String) api.getMethod("getPlayerGroup", String.class).invoke(null, player.getName());
                String targetGroup = (String) api.getMethod("getPlayerGroup", String.class).invoke(null, target.getName());
                if (setterGroup != null && !setterGroup.isEmpty() &&
                    targetGroup != null && !targetGroup.isEmpty() &&
                    setterGroup.equals(targetGroup)) {
                    bloqueia = true;
                }
            } catch (NoSuchMethodException nsme) {
                try {
                    Class<?> api = Class.forName("com.blockycraft.blockygroups.api.BlockyGroupsAPI");
                    Object res = api.getMethod("arePlayersInSameGroup", String.class, String.class)
                            .invoke(null, player.getName(), target.getName());
                    if (res instanceof Boolean && ((Boolean) res)) {
                        bloqueia = true;
                    }
                } catch (Exception ex) {
                    // fallback: não bloqueia
                }
            } catch (Exception e) {
                // fallback: não bloqueia
            }
            if (bloqueia) {
                player.sendMessage(languageManager.get(lang, "bounty.self-group"));
                return true;
            }

            if (!player.getInventory().contains(Material.IRON_INGOT, amount)) {
                player.sendMessage(languageManager.get(lang, "bounty.no-iron"));
                return true;
            }
            player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, amount));
            bountyManager.createBounty(player.getUniqueId().toString(), player.getName(), target.getUniqueId().toString(), target.getName(), amount);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("target", target.getName());
            player.sendMessage(languageManager.get(lang, "bounty.bounty-set", placeholders));

            Map<String, String> broadcastPlaceholders = new HashMap<>();
            broadcastPlaceholders.put("amount", String.valueOf(amount));
            broadcastPlaceholders.put("target", target.getName());
            broadcastPlaceholders.put("setter", player.getName());
            Bukkit.broadcastMessage(languageManager.get("en", "bounty.bounty-broadcast", broadcastPlaceholders));
            return true;
        }

        if (sub.equals("list")) {
            List<Bounty> all = bountyManager.getAllBounties();
            if (all.isEmpty()) {
                player.sendMessage(languageManager.get(lang, "bounty.no-active"));
                return true;
            }
            player.sendMessage(languageManager.get(lang, "bounty.list-header"));
            for (Bounty bounty : all) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("target", bounty.getTargetName());
                placeholders.put("amount", String.valueOf(bounty.getAmount()));
                placeholders.put("setter", bounty.getSetterName());
                player.sendMessage(languageManager.get(lang, "bounty.list-item", placeholders));
            }
            return true;
        }

        sendUsage(player, lang);
        return true;
    }

    private void sendUsage(Player player, String lang) {
        player.sendMessage(languageManager.get(lang, "bounty.help.header"));
        player.sendMessage(languageManager.get(lang, "bounty.help1"));
        player.sendMessage(languageManager.get(lang, "bounty.help2"));
        player.sendMessage(languageManager.get(lang, "bounty.help3"));
    }
}
