package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import org.bukkit.event.Listener;

public abstract class PluginListener implements Listener {

    private MobCapture plugin;

    public PluginListener(MobCapture plugin) {
        this.plugin = plugin;
    }

    public MobCapture getPlugin() {
        return this.plugin;
    }

}
