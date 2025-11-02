package com.blockycraft.blockybounty.command;

import com.blockycraft.blockybounty.BlockyBounty;
import com.blockycraft.blockybounty.manager.BountyManager;
import com.blockycraft.blockybounty.data.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BountyCommand implements CommandExecutor {

    private final BountyManager bountyManager;

    public BountyCommand(BlockyBounty plugin) {
        this.bountyManager = plugin.getBountyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.only-players",
                "&cSomente jogadores podem usar este comando."
            ));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("remover") && args.length == 2) {
            String targetName = args[1];
            Player target = Bukkit.getPlayerExact(targetName); // bounty pode ser removida só se alvo está online
            if (target == null) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.target-offline","&cJogador alvo nao encontrado online."
                ));
                return true;
            }
            List<Bounty> bounties = bountyManager.getRemovableBountiesForSetter(player.getUniqueId().toString(), target.getUniqueId().toString());
            if (bounties.isEmpty()) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.no-bounty","&cVoce nao colocou nenhuma recompensa sobre este jogador."
                ));
                return true;
            }
            int total = 0;
            for (Bounty bounty : bounties) {
                total += bounty.getAmount();
                bountyManager.removeBounty(player.getUniqueId().toString(), target.getUniqueId().toString());
            }
            player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, total));
            player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.bounty-removed",
                    "&aSua recompensa sobre {target} foi removida e &e{amount} ferros &adevolvidos."
                ).replace("{target}", target.getName())
                 .replace("{amount}", String.valueOf(total))
            );
            Bukkit.broadcastMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.remove-broadcast",
                "&a[Recompensa] {setter} removeu a recompensa sobre {target}."
            ).replace("{setter}", player.getName())
             .replace("{target}", target.getName()));
            return true;
        }

        // /bounty <alvo> <qnt>
        if (args.length == 2) {
            String targetName = args[0];
            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                        "bounty.invalid-amount","&cQuantidade invalida."
                ));
                return true;
            }
            if (amount < 1) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.amount-positive","&cUse um valor maior que zero."
                ));
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.target-offline","&cJogador alvo nao encontrado online."
                ));
                return true;
            }
            // Checar robustamente se ambos estão numa facção válida e idêntica
            boolean bloqueia = false;
            try {
                Class<?> api = Class.forName("com.blockycraft.blockyfactions.api.BlockyFactionsAPI");
                String setterFaction = (String) api.getMethod("getPlayerFaction", String.class).invoke(null, player.getName());
                String targetFaction = (String) api.getMethod("getPlayerFaction", String.class).invoke(null, target.getName());
                if (setterFaction != null && !setterFaction.isEmpty() &&
                    targetFaction != null && !targetFaction.isEmpty() &&
                    setterFaction.equals(targetFaction)) {
                    bloqueia = true;
                }
            } catch (NoSuchMethodException nsme) {
                try {
                    Class<?> api = Class.forName("com.blockycraft.blockyfactions.api.BlockyFactionsAPI");
                    Object res = api.getMethod("arePlayersInSameFaction", String.class, String.class)
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
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.self-faction","&cNao é permitido colocar recompensa em membros da propria faccao!"
                ));
                return true;
            }

            if (!player.getInventory().contains(Material.IRON_INGOT, amount)) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                        "bounty.no-iron","&cVoce nao possui ferros suficientes."
                ));
                return true;
            }
            player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, amount));
            bountyManager.createBounty(player.getUniqueId().toString(), player.getName(), target.getUniqueId().toString(), target.getName(), amount);
            player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.bounty-set","&eVoce colocou uma recompensa de {amount} ferros sobre {target}!"
            ).replace("{amount}", String.valueOf(amount))
             .replace("{target}", target.getName()));
            Bukkit.broadcastMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.bounty-broadcast", "&c[Recompensa] {setter} colocou &e{amount} ferros &cpela cabeca de {target}!"
            ).replace("{amount}", String.valueOf(amount))
             .replace("{target}", target.getName())
             .replace("{setter}", player.getName()));
            return true;
        }

        if (sub.equals("list")) {
            List<Bounty> all = bountyManager.getAllBounties();
            if (all.isEmpty()) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.no-active","&cNenhuma recompensa ativa."
                ));
                return true;
            }
            player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                "bounty.list-header","&eRecompensas &aativas:"
            ));
            for (Bounty bounty : all) {
                player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
                    "bounty.list-item", "&a- &a{target}: &e{amount} ferros &cpor {setter}"
                ).replace("{target}", bounty.getTargetName())
                 .replace("{amount}", String.valueOf(bounty.getAmount()))
                 .replace("{setter}", bounty.getSetterName()));
            }
            return true;
        }

        sendUsage(player);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
            "bounty.help.header","&f--- Comandos de &dBounty &f---"
        ));
        player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
            "bounty.help1","&d/bounty <jogador> <quantidade> &7- coloca uma recompensa em um jogador"
        ));
        player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
            "bounty.help2","&d/bounty remover <jogador> &7- remove a recompensa"
        ));
        player.sendMessage(BlockyBounty.getInstance().getMessageManager().get(
            "bounty.help3","&d/bounty list &7- lista as recompensas ativas"
        ));
    }
}
