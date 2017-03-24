package de.codehat.mobcapture.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

public class InventoryUtil {

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom RND = new SecureRandom();

    public static String toBase64(Inventory inventory) {
        return toBase64(inventory.getContents());
    }

    public static String toBase64(ItemStack itemstack) {
        ItemStack[] arr = new ItemStack[1];
        arr[0] = itemstack;
        return toBase64(arr);
    }

    public static String toBase64(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);

            for (ItemStack stack : contents) {
                dataOutput.writeObject(stack);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static Inventory inventoryFromBase64(String data) throws IOException {
        ItemStack[] stacks = stacksFromBase64(data);
        Inventory inventory = Bukkit.createInventory(null, (int) Math.ceil(stacks.length / 9D) * 9);

        for (int i = 0; i < stacks.length; i++) {
            inventory.setItem(i, stacks[i]);
        }

        return inventory;
    }

    public static ItemStack[] stacksFromBase64(String data) throws IOException {
        try {
            if (data == null || Base64Coder.decodeLines(data) == null) return new ItemStack[]{};
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] stacks = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return stacks;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    // Another solution

    static String randomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append(CHARS.charAt(RND.nextInt(CHARS.length())));
        return sb.toString();
    }

    public static void saveInventory(String datafolderPath, Inventory inventory, @Nullable String id) throws IOException {
        if (id == null) id = randomString(8);
        File f = new File(datafolderPath + File.separator + "inventories", id + ".inv.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        //c.set("inventory.armor", inventory.getArmorContents());
        c.set("contents", inventory.getContents());
        c.save(f);
    }

    public static ItemStack[] restoreInventory(String datafolderPath, String id) throws IOException {
        File f = new File(datafolderPath + File.separator + "inventories", id + ".inv.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        //ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        //p.getInventory().setArmorContents(content);
        return ((List<ItemStack>) c.get("contents")).toArray(new ItemStack[0]);
        //p.getInventory().setContents(content);
    }

    public static void saveEqipment(String datafolderPath, EntityEquipment equipment, @Nullable String id) throws IOException {
        if (id == null) id = randomString(8);
        File f = new File(datafolderPath + File.separator + "equipments", id + ".eqpm.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("hand.main", equipment.getItemInMainHand());
        c.set("hand.off", equipment.getItemInOffHand());
        c.set("equipment", equipment.getArmorContents());
        c.set("chances.hand.main", equipment.getItemInMainHandDropChance());
        c.set("chances.hand.off", equipment.getItemInOffHandDropChance());
        c.set("chances.armor.helmet", equipment.getHelmetDropChance());
        c.set("chances.armor.chestplate", equipment.getChestplateDropChance());
        c.set("chances.armor.leggings", equipment.getLeggingsDropChance());
        c.set("chances.armor.boots", equipment.getBootsDropChance());
        c.save(f);
    }

    public static void restoreEquipment(String datafolderPath, String id, EntityEquipment equipment) {
        File f = new File(datafolderPath + File.separator + "equipments", id + ".eqpm.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] armorContents = ((List<ItemStack>) c.get("equipment")).toArray(new ItemStack[0]);
        equipment.setArmorContents(armorContents);
        equipment.setItemInMainHand(c.getItemStack("hand.main"));
        equipment.setItemInOffHand(c.getItemStack("hand.off"));
        equipment.setItemInMainHandDropChance((float) c.get("chances.hand.main"));
        equipment.setItemInOffHandDropChance((float) c.get("chances.hand.off"));
        equipment.setHelmetDropChance((float) c.get("chances.armor.helmet"));
        equipment.setChestplateDropChance((float) c.get("chances.armor.chestplate"));
        equipment.setLeggingsDropChance((float) c.get("chances.armor.leggings"));
        equipment.setBootsDropChance((float) c.get("chances.armor.boots"));
    }
}
