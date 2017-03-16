package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerCaptureMobEvent;
import de.codehat.mobcapture.events.PlayerSpawnCapturedMobEvent;
import de.codehat.mobcapture.util.Message;
import de.codehat.mobcapture.util.TriConsumer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {

    private MobCapture plugin;
    //TODO: Remove 'TriConsumer' and set to 'BiConsumer' by removing the String (value) and get it from the map.
    private Map<String, TriConsumer<Map<String, String>, LivingEntity, String>> attributeFunctions = new HashMap<>();

    public PlayerListener(MobCapture plugin) {
        this.plugin = plugin;
        this.setupAttributeFunctions();
    }

    private void setupAttributeFunctions() {
        // Mob:
        this.attributeFunctions.put("mob", (m, e, s) -> {
           switch (EntityType.valueOf(s)) {
               case OCELOT:
                   ((Ocelot) e).setCatType(Ocelot.Type.valueOf(m.get("cat type")));
                   break;
               case HORSE:
                   ((Horse) e).setStyle(Horse.Style.valueOf(m.get("style")));
                   ((Horse) e).setColor(Horse.Color.valueOf(m.get("horse color")));
                   break;
               case VILLAGER:
                   ((Villager) e).setProfession(Villager.Profession.valueOf(m.get("profession")));
                   break;
               case SHEEP:
                   ((Sheep) e).setSheared(Boolean.valueOf(m.get("sheared")));
                   break;
               case WOLF:
                   ((Wolf) e).setCollarColor(DyeColor.valueOf(m.get("collar color")));
                   ((Wolf) e).setAngry(Boolean.valueOf(m.get("angry")));
                   break;
               case ZOMBIE_VILLAGER:
                   ((ZombieVillager) e).setVillagerProfession(Villager.Profession.valueOf(m.get("profession")));
                   break;
               case SLIME:
                   ((Slime) e).setSize(Integer.valueOf(m.get("size")));
                   break;
               case LLAMA:
                   ((Llama) e).setColor(Llama.Color.valueOf(m.get("llama color")));
                   ((Llama) e).setStrength(Integer.valueOf(m.get("strength")));
                   break;
               case CREEPER:
                   ((Creeper) e).setPowered(Boolean.valueOf(m.get("powered")));
                   break;
               case ENDERMAN:
                   ((Enderman) e).setCarriedMaterial(new MaterialData(Material.valueOf(m.get("block"))));
                   break;
               case RABBIT:
                   ((Rabbit) e).setRabbitType(Rabbit.Type.valueOf(m.get("rabbit type")));
                   break;
           }
        });

        // Name
        this.attributeFunctions.put("name", (m, e, s) -> e.setCustomName(s));

        // Health
        this.attributeFunctions.put("health", (m, e, s) -> {
            String[] health = s.replace(",", ".").split("/");
            double currentHealth = Double.valueOf(health[0]);
            double maxHealth = Double.valueOf(health[1]);

            e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            e.setHealth(currentHealth);
        });

        // Age
        this.attributeFunctions.put("age", (m, e, s) -> {
            if (s.equalsIgnoreCase("baby")) {
                ((Ageable) e).setBaby();
            } else {
                ((Ageable) e).setAdult();
            }
        });

        // Baby
        this.attributeFunctions.put("baby", (m, e, s) -> {
           if (Boolean.valueOf(s)) {
               ((Zombie) e).setBaby(true);
           } else {
               ((Zombie) e).setBaby(false);
           }
        });

        // Color
        this.attributeFunctions.put("color", (m, e, s) -> {
            ((Colorable) e).setColor(DyeColor.valueOf(s));
        });

        // Tamed
        this.attributeFunctions.put("tamed", (m, e, s) -> {
            ((Tameable) e).setTamed(Boolean.valueOf(s));
        });

        // Owner
        this.attributeFunctions.put("uuid", (m, e, s) -> {
            ((Tameable) e).setOwner(this.plugin.getServer().getOfflinePlayer(UUID.fromString(s)));
        });
    }

    @EventHandler
    public void onPlayerEggThrowEvent(PlayerEggThrowEvent event) {
        if (this.plugin.getEggStorage().contains(event.getEgg())) {
            event.setHatching(false);
            this.plugin.getEggStorage().remove(event.getEgg());
        }
    }

    @EventHandler
    public void onPlayerSpawnMob(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if(!event.getMaterial().equals(Material.MONSTER_EGG)) {
            return;
        }

        if (event.getItem().hasItemMeta() && !event.getItem().getItemMeta().hasLore()) {
            return;
        }
        Player player = event.getPlayer();
        Location spawnMobLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
        SpawnEggMeta spawnEggMeta = (SpawnEggMeta) event.getItem().getItemMeta();
        EntityType entityType = spawnEggMeta.getSpawnedType();


        PlayerSpawnCapturedMobEvent playerSpawnCapturedMobEvent = new PlayerSpawnCapturedMobEvent(event.getItem(),
                entityType, player, spawnMobLocation);
        Bukkit.getPluginManager().callEvent(playerSpawnCapturedMobEvent);

        if (playerSpawnCapturedMobEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        ItemStack spawnEgg = playerSpawnCapturedMobEvent.getSpawnEggStack();

        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            if (spawnEgg.getAmount() > 1) {
                spawnEgg.setAmount(spawnEgg.getAmount() - 1);
            } else {
                player.getInventory().clear(player.getInventory().getHeldItemSlot());
            }
        }

        List<String> lore = spawnEgg.getItemMeta().getLore();
        List<String> noColorLore = lore.stream().map(ChatColor::stripColor).collect(Collectors.toList());
        Map<String, String> entityDataMap = noColorLore.stream().collect(Collectors.toMap(
                s -> s.split(": ")[0].trim().toLowerCase(),
                s -> s.split(": ")[1].trim()
        ));

        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(spawnMobLocation, entityType);

        for (String s : entityDataMap.keySet()) {
            MobCapture.logger.info(s + ":: " + entityDataMap.get(s));
            if (this.attributeFunctions.containsKey(s)) {
                this.attributeFunctions.get(s).accept(entityDataMap, entity, entityDataMap.get(s));
            }
        }
        event.setCancelled(true);
    }

}
