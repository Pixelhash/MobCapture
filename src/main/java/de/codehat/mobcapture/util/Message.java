package de.codehat.mobcapture.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Message {

    private static final String PLUGIN_LOGO = "&9[&eMobCapture&9] ";

    /**
     * Sends a message with the plugin logo.
     *
     * @param sender  CommandSender.
     * @param message Message to send.
     */
    public static void sendWithLogo(CommandSender sender, String message) {
        sender.sendMessage(replaceColors(PLUGIN_LOGO + message));
    }

    /**
     * Sends a blank message.
     *
     * @param sender  CommandSender.
     * @param message Message to send.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(replaceColors(message));
    }

    /**
     * Creates a colored string.
     *
     * @param message Message to translate color codes.
     * @return A translated string.
     */
    public static String replaceColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
