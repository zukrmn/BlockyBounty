package com.blockycraft.blockybounty.listener;

import com.blockycraft.blockybounty.BlockyBounty;
import com.blockycraft.blockybounty.data.Bounty;
import com.blockycraft.blockybounty.manager.BountyManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.List;

public class BountyDeathListener extends EntityListener {

    private final BountyManager bountyManager;

    public BountyDeathListener(BlockyBounty plugin) {
        this.bountyManager = plugin.getBountyManager();
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // Compatível com Bukkit 1060: procura killer pelo último golpe
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

        // Checagem robusta de facção: só bloqueia recompensa se ambos estiverem em facção válida/igual
        boolean bloqueia = false;
        try {
            Class<?> api = Class.forName("com.blockycraft.blockyfactions.api.BlockyFactionsAPI");
            String killerFaction = (String) api.getMethod("getPlayerFaction", String.class).invoke(null, killer.getName());
            String victimFaction = (String) api.getMethod("getPlayerFaction", String.class).invoke(null, victim.getName());
            if (killerFaction != null && !killerFaction.isEmpty() &&
                victimFaction != null && !victimFaction.isEmpty() &&
                killerFaction.equals(victimFaction)) {
                bloqueia = true;
            }
        } catch (NoSuchMethodException nsme) {
            // fallback: tenta arePlayersInSameFaction (modo legacy) se não houver getPlayerFaction
            try {
                Class<?> api = Class.forName("com.blockycraft.blockyfactions.api.BlockyFactionsAPI");
                Object res = api.getMethod("arePlayersInSameFaction", String.class, String.class)
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

        if (bloqueia) {
            killer.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.killer-faction",
                "&cJogadores da mesma faccao nao podem resgatar recompensa. A recompensa permanecera ativa."
            ));
            return;
        }

        int totalBounty = 0;
        for (Bounty bounty : bountiesOnVictim) {
            totalBounty += bounty.getAmount();
        }

        if (totalBounty > 0) {
            killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, totalBounty));
            Bukkit.broadcastMessage(
                    BlockyBounty.getInstance().getMessageManager().get(
                        "bounty.payment",
                        "&c[Bounty] {killer} eliminou {target} e recebeu &e{amount} ferros &cde recompensa!"
                    )
                    .replace("{killer}", killer.getName())
                    .replace("{target}", victim.getName())
                    .replace("{amount}", String.valueOf(totalBounty))
            );
            // Remove todas as bounties sobre o alvo (independente do setter estar online)
            bountyManager.removeAllBountiesForTarget(victim.getUniqueId().toString());
        }
    }
}
