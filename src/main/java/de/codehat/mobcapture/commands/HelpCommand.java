package de.codehat.mobcapture.commands;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCommand extends AbstractCommand{

    public HelpCommand(MobCapture plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("mobcapture.help")) {
            Message.sendWithLogo(sender, "&cYou don't have permission.");
        } else {
            Message.send(sender, "&8+----------&9[&eMobCapture&9]&8----------+");
            Message.send(sender, "&7/mc help &e--- &7Shows this page.");
            Message.send(sender, "&7/mc reload &e--- &7Reloads the config.");
            Message.send(sender, "&8+------------------------------+");
        }
    }
}
