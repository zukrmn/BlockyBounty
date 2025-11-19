package com.blockycraft.blockybounty.listener;

import com.blockycraft.blockybounty.BlockyBounty;
import com.blockycraft.blockybounty.data.Bounty;
import com.blockycraft.blockybounty.manager.BountyManager;
import com.blockycraft.blockybounty.lang.LanguageManager;
import com.blockycraft.blockybounty.geoip.GeoIPManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BountyDeathListener implements Listener {
    private final BountyManager bountyManager;
    private final LanguageManager languageManager;
    private final GeoIPManager geoIPManager;

    public BountyDeathListener(BlockyBounty plugin) {
        this.bountyManager = plugin.getBountyManager();
        this.languageManager = plugin.getLanguageManager();
        this.geoIPManager = plugin.getGeoIPManager();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player killer = null;
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntity = (EntityDamageByEntityEvent) lastDamage;
            if (damageByEntity.getDamager() instanceof Player) {
                killer = (Player) damageByEntity.getDamager();
            }
        }

        if (killer == null || killer.equals(victim)) return;

        List<Bounty> bountiesOnVictim = bountyManager.getBountiesForTarget(victim.getUniqueId().toString());
        if (bountiesOnVictim == null || bountiesOnVictim.isEmpty()) return;

        boolean bloqueia = false;
        try {
            Class<?> api = Class.forName("com.blockycraft.blockygroups.api.BlockyGroupsAPI");
            String killerGroup = (String) api.getMethod("getPlayerGroup", String.class).invoke(null, killer.getName());
            String victimGroup = (String) api.getMethod("getPlayerGroup", String.class).invoke(null, victim.getName());
            if (killerGroup != null && !killerGroup.isEmpty()
                    && victimGroup != null && !victimGroup.isEmpty()
                    && killerGroup.equals(victimGroup)) {
                bloqueia = true;
            }
        } catch (NoSuchMethodException nsme) {
            try {
                Class<?> api = Class.forName("com.blockycraft.blockygroups.api.BlockyGroupsAPI");
                Object res = api.getMethod("arePlayersInSameGroup", String.class, String.class)
                        .invoke(null, killer.getName(), victim.getName());
                if (res instanceof Boolean && ((Boolean) res)) {
                    bloqueia = true;
                }
            } catch (Exception ex) {
                // fallback: se erro, não bloqueia (paga)
            }
        } catch (Exception e) {
            // fallback: não bloqueia, paga recompensa
        }

        String lang = geoIPManager.getPlayerLanguage(killer);

        if (bloqueia) {
            killer.sendMessage(languageManager.get(lang, "bounty.killer-group"));
            return;
        }

        int totalBounty = 0;
        for (Bounty bounty : bountiesOnVictim) {
            totalBounty += bounty.getAmount();
        }
        if (totalBounty > 0) {
            killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, totalBounty));
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("killer", killer.getName());
            placeholders.put("target", victim.getName());
            placeholders.put("amount", String.valueOf(totalBounty));

            Bukkit.broadcastMessage(languageManager.get("en", "bounty.payment", placeholders));
            bountyManager.removeAllBountiesForTarget(victim.getUniqueId().toString());
        }
    }
}
