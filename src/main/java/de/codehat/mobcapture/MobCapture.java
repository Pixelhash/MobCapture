package de.codehat.mobcapture;

import de.codehat.mobcapture.commands.CommandManager;
import de.codehat.mobcapture.commands.HelpCommand;
import de.codehat.mobcapture.commands.ReloadConfigCommand;
import de.codehat.mobcapture.listeners.EntityListener;
import de.codehat.mobcapture.listeners.MobCaptureListener;
import de.codehat.mobcapture.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Egg;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MobCapture extends JavaPlugin {

    // Our static logger for easy access
    public static Logger logger;
    private static Economy econ = null;

    private CommandManager commandManager = new CommandManager(this);
    private List<Egg> eggStorage = new ArrayList<>();

    @Override
    public void onDisable() {
        // Log "disable" message
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        logger.info(String.format("Version %s by %s disabled.", pluginDescriptionFile.getVersion(),
                pluginDescriptionFile.getAuthors().get(0)));
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();

        // Setup Vault
        if (!setupEconomy() ) {
            logger.severe("Vault not found! Please install it and restart the server!");
        }

        // Save our default config if not exists
        saveDefaultConfig();

        this.registerListeners();
        this.registerCommands();

        // Log enable message
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        logger.info(String.format("Version %s by %s enabled.", pluginDescriptionFile.getVersion(),
                pluginDescriptionFile.getAuthors().get(0)));
    }

    private void registerCommands() {
        HelpCommand helpCommand = new HelpCommand(this);
        this.commandManager.registerCommand("", helpCommand);
        this.commandManager.registerCommand("help", helpCommand);
        this.commandManager.registerCommand("reload", new ReloadConfigCommand(this));

        this.getCommand("mc").setExecutor(this.commandManager);
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new MobCaptureListener(this), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public List<Egg> getEggStorage() {
        return this.eggStorage;
    }
}
