package de.codehat.mobcapture.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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

    public static String saveInventory(String datafolderPath, Inventory inventory, @Nullable String id) throws IOException {
        if (id == null) id = randomString(8);
        File f = new File(datafolderPath + File.separator + "inventories", id + ".inv.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        //c.set("inventory.armor", inventory.getArmorContents());
        c.set("contents", inventory.getContents());
        c.save(f);
        return id;
    }

    public static ItemStack[] restoreInventory(String datafolderPath, String id) {
        File f = new File(datafolderPath + File.separator + "inventories", id + ".inv.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        //ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        //p.getInventory().setArmorContents(content);
        return ((List<ItemStack>) c.get("contents")).toArray(new ItemStack[0]);
        //p.getInventory().setContents(content);
    }

    public static String saveEqipment(String datafolderPath, EntityEquipment equipment, @Nullable String id) throws IOException {
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
        return id;
    }

    public static void restoreEquipment(String datafolderPath, String id, EntityEquipment equipment) {
        File f = new File(datafolderPath + File.separator + "equipments", id + ".eqpm.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] armorContents = ((List<ItemStack>) c.get("equipment")).toArray(new ItemStack[0]);
        equipment.setArmorContents(armorContents);
        equipment.setItemInMainHand(c.getItemStack("hand.main"));
        equipment.setItemInOffHand(c.getItemStack("hand.off"));
        equipment.setItemInMainHandDropChance(((Double) c.getDouble("chances.hand.main")).floatValue());
        equipment.setItemInOffHandDropChance(((Double) c.getDouble("chances.hand.off")).floatValue());
        equipment.setHelmetDropChance(((Double) c.getDouble("chances.armor.helmet")).floatValue());
        equipment.setChestplateDropChance(((Double) c.getDouble("chances.armor.chestplate")).floatValue());
        equipment.setLeggingsDropChance(((Double) c.getDouble("chances.armor.leggings")).floatValue());
        equipment.setBootsDropChance(((Double) c.getDouble("chances.armor.boots")).floatValue());
    }

    public static Inventory getEquipmentInventory(String datafolderPath, String id) {
        File f = new File(datafolderPath + File.separator + "equipments", id + ".eqpm.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, Message.replaceColors("&9Equipment"));
        ItemStack[] armorContents = ((List<ItemStack>) c.get("equipment")).toArray(new ItemStack[0]);
        for (int i = 0; i < armorContents.length; i++) {
            inv.setItem(i, armorContents[i]);
        }
        inv.setItem(9, c.getItemStack("hand.main"));
        inv.setItem(10, c.getItemStack("hand.off"));
        return inv;
    }

    /**
     * Hides text in color codes
     *
     * @param text The text to hide
     * @return The hidden text
     */
    @Nonnull
    public static String hideText(@Nonnull String text) {
        Objects.requireNonNull(text, "text can not be null!");

        StringBuilder output = new StringBuilder();

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);

        for (char c : hex.toCharArray()) {
            output.append(ChatColor.COLOR_CHAR).append(c);
        }

        return output.toString();
    }

    /**
     * Reveals the text hidden in color codes
     *
     * @param text The hidden text
     * @throws IllegalArgumentException if an error occurred while decoding.
     * @return The revealed text
     */
    @Nonnull
    public static String revealText(@Nonnull String text) {
        Objects.requireNonNull(text, "text can not be null!");

        if (text.isEmpty()) {
            return text;
        }

        char[] chars = text.toCharArray();

        char[] hexChars = new char[chars.length / 2];

        IntStream.range(0, chars.length)
                .filter(value -> value % 2 != 0)
                .forEach(value -> hexChars[value / 2] = chars[value]);

        try {
            return new String(Hex.decodeHex(hexChars), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't decode text", e);
        }
    }
}
