package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerCaptureMobEvent;
import de.codehat.mobcapture.util.Message;
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
    public void onCapturingMob(PlayerCaptureMobEvent event) {
        Player player = event.getPlayer();

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

        // Check if Vault is enabled
        if (MobCapture.getEconomy() != null) {
            // Check if the player has enough money
            double costs = this.getCosts(event.getEntity().getType());
            MobCapture.logger.info(String.valueOf(costs) + " : " + this.plugin.getConfig().getDouble("capturing.mobs." + event.getEntity().getType().toString() + ".costs"));
            if (MobCapture.getEconomy().getBalance(player) < costs) {
                Message.sendWithLogo(player, "&cYou don't have enough money.");
                event.setCancelled(true);
                return;
            }
            MobCapture.getEconomy().withdrawPlayer(player, costs);
        }
    }

    private Double getCosts(EntityType entityType) {
        if (this.plugin.getConfig().get("capturing.mobs." + entityType.toString() + ".costs", null) == null) {
            return this.plugin.getConfig().getDouble("capturing.mobs.default.costs");
        }
        return this.plugin.getConfig().getDouble("capturing.mobs." + entityType.toString() + ".costs");
    }

}
