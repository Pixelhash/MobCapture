package de.codehat.mobcapture.dependencies;

import de.codehat.mobcapture.MobCapture;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultDependency extends MCDependency {

    private Economy economy = null;

    public VaultDependency(MobCapture plugin) {
        super(plugin);
        this.setupEconomy();
    }

    private boolean setupEconomy() {
        if (this.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = this.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return this.economy != null;
    }

    public Economy getEconomy() {
        return this.economy;
    }
}
