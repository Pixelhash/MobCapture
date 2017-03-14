package de.codehat.mobcapture.commands;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandManager implements CommandExecutor {

    private MobCapture plugin;
    private HashMap<String, AbstractCommand> commandDatabase = new HashMap<>();

    public CommandManager(MobCapture plugin) {
        this.plugin = plugin;
    }

    public boolean registerCommand(String name, AbstractCommand command) {
        if (!this.existsCommand(name)) {
            this.commandDatabase.put(name.toLowerCase(), command);
            return true;
        }
        return false;
    }

    public boolean existsCommand(String name) {
        return this.commandDatabase.containsKey(name);
    }

    public AbstractCommand getCommand(String name) {
        return this.commandDatabase.get(name);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            this.getCommand("").onCommand(sender, cmd, label, args);
        } else if (this.existsCommand(args[0])) {
            this.getCommand(args[0]).onCommand(sender, cmd, label, args);
        } else {
            Message.sendWithLogo(sender, "&cThis command doesn't exist.");
        }
        return true;
    }



}
