package de.codehat.mobcapture.commands;

import de.codehat.mobcapture.MobCapture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {

    private MobCapture plugin;

    public AbstractCommand(MobCapture plugin) {
        this.plugin = plugin;
    }

    public MobCapture getPlugin() {
        return this.plugin;
    }

    public abstract void onCommand(CommandSender sender, Command cmd, String label, String[] args);

}
