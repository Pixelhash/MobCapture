package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerSpawnCapturedMobEvent;
import de.codehat.mobcapture.util.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpawnCapturedMobListener extends PluginListener {

    public SpawnCapturedMobListener(MobCapture plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerSpawnCapturedMob(PlayerSpawnCapturedMobEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("mobcapture.spawn." + event.getEntityType().toString().toLowerCase())) {
            Message.sendWithLogo(player, "&cYou don't have permission to spawn this mob.");
            event.setCancelled(true);
        }
    }
}
