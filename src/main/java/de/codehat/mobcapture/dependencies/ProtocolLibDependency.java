package de.codehat.mobcapture.dependencies;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import de.codehat.mobcapture.MobCapture;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.List;

public class ProtocolLibDependency extends Dependency {

    private ProtocolManager protocolManager;

    public ProtocolLibDependency(MobCapture plugin) {
        super(plugin);
        this.setupPorotocolLib();
        //this.registerListeners();
    }

    private void setupPorotocolLib() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    private void registerListeners() {
        this.protocolManager.addPacketListener(new PacketAdapter(this.getPlugin(),
                ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    PacketContainer packet = event.getPacket().deepClone();
                    StructureModifier<ItemStack> sm = packet
                            .getItemModifier();
                    for (int j = 0; j < sm.size(); j++) {
                        if (sm.getValues().get(j) != null) {
                            ItemStack item = sm.getValues().get(j);
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null && itemMeta.hasLore()) {
                                List<String> lore = itemMeta.getLore();
                                Iterator<String> it = lore.iterator();
                                while (it.hasNext()) {
                                    String stripped = ChatColor.stripColor(it.next());
                                    if (stripped.startsWith("UUID:") || stripped.startsWith("Inventory:") || stripped.startsWith("Equipment:")) {
                                        it.remove();
                                    }
                                }
                                itemMeta.setLore(lore);
                                item.setItemMeta(itemMeta);
                            }
                        }
                    }
                    event.setPacket(packet);
                }
                if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                    PacketContainer packet = event.getPacket().deepClone();
                    StructureModifier<ItemStack[]> sm = packet
                            .getItemArrayModifier();
                    for (int j = 0; j < sm.size(); j++) {
                        for (int i = 0; i < sm.getValues().size(); i++) {
                            if (sm.getValues().get(j)[i] != null) {
                                ItemStack item = sm.getValues().get(j)[i];
                                ItemMeta itemMeta = item.getItemMeta();
                                if (itemMeta != null && itemMeta.hasLore()) {
                                    List<String> lore = itemMeta.getLore();
                                    Iterator<String> it = lore.iterator();
                                    while (it.hasNext()) {
                                        String stripped = ChatColor.stripColor(it.next());
                                        if (stripped.startsWith("UUID:") || stripped.startsWith("Inventory:") || stripped.startsWith("Equipment:")) {
                                            it.remove();
                                        }
                                    }
                                    itemMeta.setLore(lore);
                                    item.setItemMeta(itemMeta);
                                }
                            }
                        }
                    }
                    event.setPacket(packet);
                }
            }
        });
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
