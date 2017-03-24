package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerCaptureMobEvent;
import de.codehat.mobcapture.util.Message;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobCaptureListener implements Listener {

    private MobCapture plugin;

    public MobCaptureListener(MobCapture plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCaptureMob(PlayerCaptureMobEvent event) {
        Player player = event.getPlayer();

        //TODO: Move back into 'EntityListener' next to the disabled world check.
        // Check if capture this kind of mob is disabled
        if (!this.plugin.getConfig().contains("capturing.mobs."
                + event.getEntity().getType().toString(), true)
                || !this.plugin.getConfig().getBoolean("capturing.mobs."
                + event.getEntity().getType().toString() + ".enable")) {
            event.setCancelled(true);
            return;
        }

        // Check if player has required permission
        if (!player.hasPermission("mobcapture.capture." + event.getEntity().getType().toString().toLowerCase())) {
            Message.sendWithLogo(player, "&cYou don't have permission to catch this mob.");
            event.setCancelled(true);
            return;
        }

        // Check chance is enabled
        if (this.plugin.getConfig().getBoolean("capturing.chance")) {
            double rnd = Math.random();
            double chance = this.plugin.getConfig().getDouble("capturing.mobs." + event.getEntity().getType().toString() + ".chance");
            if (chance == 0) {
                chance = this.plugin.getConfig().getDouble("capturing.mobs.default.chance");
            }
            if (chance != -1) {
                if (!(rnd < chance)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 0.2F);
                    Message.sendWithLogo(player, "&cThe mob broke out of your egg.");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check if Vault is enabled
        if (this.plugin.isEconomyAvailable()) {
            // Check if the player has enough money
            double costs = this.getCosts(event.getEntity().getType());
            MobCapture.logger.info(String.valueOf(costs) + " : " + this.plugin.getConfig().getDouble("capturing.mobs." + event.getEntity().getType().toString() + ".costs"));
            if (this.plugin.getVaultDependency().getEconomy().getBalance(player) < costs) {
                Message.sendWithLogo(player, "&cYou don't have enough money.");
                event.setCancelled(true);
                return;
            }
            this.plugin.getVaultDependency().getEconomy().withdrawPlayer(player, costs);
        }
    }

    private Double getCosts(EntityType entityType) {
        if (this.plugin.getConfig().get("capturing.mobs." + entityType.toString() + ".costs", null) == null) {
            return this.plugin.getConfig().getDouble("capturing.mobs.default.costs");
        }
        return this.plugin.getConfig().getDouble("capturing.mobs." + entityType.toString() + ".costs");
    }

}
