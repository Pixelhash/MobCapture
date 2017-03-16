package de.codehat.mobcapture;

import de.codehat.mobcapture.commands.CommandManager;
import de.codehat.mobcapture.commands.HelpCommand;
import de.codehat.mobcapture.commands.ReloadConfigCommand;
import de.codehat.mobcapture.dependencies.VaultDependency;
import de.codehat.mobcapture.dependencies.WorldGuardDependency;
import de.codehat.mobcapture.listeners.EntityListener;
import de.codehat.mobcapture.listeners.MobCaptureListener;
import de.codehat.mobcapture.listeners.PlayerListener;
import de.codehat.mobcapture.listeners.SpawnCapturedMobListener;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Enderman;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MobCapture extends JavaPlugin {

    // Our static logger for easy access
    public static Logger logger;

    private VaultDependency vaultDependency = null;
    private WorldGuardDependency worldGuardDependency = null;
    private CommandManager commandManager = new CommandManager(this);
    private List<Egg> eggStorage = new ArrayList<>();
    private List<Enderman> endermanStorage = new ArrayList<>();

    @Override
    public void onDisable() {
        // Log "disable" message
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        logger.info(String.format("Version %s by %s disabled.", pluginDescriptionFile.getVersion(),
                pluginDescriptionFile.getAuthors().get(0)));
    }

    @Override
    public void onLoad() {
        logger = this.getLogger();
        // Setup WorldGuard
        try {
            this.worldGuardDependency = new WorldGuardDependency(this);
        } catch (NoClassDefFoundError e) {
            logger.severe("WorldGuard is missing! Disabling 'mob protection in regions' feature...");
        }
    }

    @Override
    public void onEnable() {
        // Save our default config if not exists
        saveDefaultConfig();

        // Setup Vault / Economy support
        try {
            this.vaultDependency = new VaultDependency(this);
        } catch (NoClassDefFoundError e) {
            logger.severe("Vault is missing! Disabling 'economy' feature...");
        }

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
        this.getServer().getPluginManager().registerEvents(new SpawnCapturedMobListener(this), this);
    }

    public VaultDependency getVaultDependency() {
        return vaultDependency;
    }

    public WorldGuardDependency getWorldGuardDependency() {
        return worldGuardDependency;
    }

    public List<Egg> getEggStorage() {
        return this.eggStorage;
    }

    public List<Enderman> getEndermanStorage() {
        return endermanStorage;
    }
}
