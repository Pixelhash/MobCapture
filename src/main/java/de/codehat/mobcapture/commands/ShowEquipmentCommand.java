package de.codehat.mobcapture.commands;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.util.InventoryUtil;
import de.codehat.mobcapture.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShowEquipmentCommand extends AbstractCommand {

    public ShowEquipmentCommand(MobCapture plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendWithLogo(sender, "&cCan only be executed by a player!");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("mobcapture.show.equipment")) {
            Message.sendWithLogo(sender, "&cYou don't have permission.");
            return;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null) {
            Message.sendWithLogo(player, "&cYou don't have an item in your main hand!");
            return;
        }
        if (!itemInHand.getType().equals(Material.MONSTER_EGG)) {
            Message.sendWithLogo(player, "&cYou have to hold a monster egg in your hand!");
            return;
        }
        if (!itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasLore()) {
            Message.sendWithLogo(player, "&cInvalid monster egg (not a custom one)!");
            return;
        }
        List<String> lore = itemInHand.getItemMeta().getLore();
        int index = this.checkForKey("Equipment: Yes", lore);
        if (index == -1) {
            Message.sendWithLogo(player, "&cEgg hasn't an inventory!");
            return;
        }
        String eqpmId = InventoryUtil.revealText(lore.get(index).split(":")[1].trim().split("Yes")[1]);
        Inventory inv = InventoryUtil.getEquipmentInventory(this.getPlugin().getDataFolder().getAbsolutePath(), eqpmId);
        player.openInventory(inv);

    }

    private int checkForKey(String key, List<String> li) {
        for (int i = 0; i < li.size(); i++) {
            if (ChatColor.stripColor(li.get(i)).contains(key)) return i;
        }
        return -1;
    }
}
