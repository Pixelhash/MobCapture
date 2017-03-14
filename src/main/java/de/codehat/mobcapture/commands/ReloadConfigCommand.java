package de.codehat.mobcapture.commands;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadConfigCommand extends AbstractCommand {

    public ReloadConfigCommand(MobCapture plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("mobcapture.reload")) {
            Message.sendWithLogo(sender, "&cYou don't have permission.");
        } else {
            this.getPlugin().reloadConfig();
            Message.sendWithLogo(sender, "&aReloaded config successfully.");
        }
    }
}
