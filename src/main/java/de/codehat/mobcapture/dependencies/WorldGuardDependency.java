package de.codehat.mobcapture.dependencies;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.codehat.mobcapture.MobCapture;
import org.bukkit.plugin.Plugin;

public class WorldGuardDependency extends Dependency {

    private WorldGuardPlugin worldGuardPlugin = null;

    // StateFlag with the name "capture-mobs", which defaults to "allow"
    private Flag captureMobs = null;

    public WorldGuardDependency(MobCapture plugin) {
        super(plugin);
        this.setupWorldGuard();
        this.registerWorldGuardFlags();
    }

    private boolean setupWorldGuard() {
        if (this.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            return false;
        }
        Plugin plugin = this.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            return false;
        }
        worldGuardPlugin = (WorldGuardPlugin) plugin;
        return true;
    }

    private void registerWorldGuardFlags() {
        captureMobs = new StateFlag("capture-mobs", true);
        FlagRegistry registry = worldGuardPlugin.getFlagRegistry();
        try {
            // Register our flag with the registry
            registry.register(captureMobs);
        } catch (FlagConflictException e) {
            e.printStackTrace();
        }
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public Flag getCaptureMobs() {
        return captureMobs;
    }
}
