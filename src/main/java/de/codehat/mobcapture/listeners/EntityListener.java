package de.codehat.mobcapture.listeners;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerCaptureMobEvent;
import de.codehat.mobcapture.util.InventoryUtil;
import de.codehat.mobcapture.util.Message;
import de.codehat.mobcapture.util.MonsterType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.Colorable;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityListener extends PluginListener {

    private DecimalFormat decimalFormat = new DecimalFormat("#.0");
    private final Random random = new Random();

    public EntityListener(MobCapture plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDamageWithEgg(EntityDamageByEntityEvent event) {
        // Check if entity is a living entity
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        // The damaged living entity
        LivingEntity entity = (LivingEntity) event.getEntity();

        // If not hit by an egg return
        if (!(event.getDamager() instanceof Egg)) {
            return;
        }
        // The egg, which caused damage
        Egg egg = (Egg) event.getDamager();

        // Check if the entity can be spawned by MobCapture
        MonsterType monsterType = MonsterType.getEggType(entity);
        // If monster cannot be found (spawned) return
        if (monsterType == null) {
            return;
        }

        // Check if the world the entity is living in is disabled. If disabled return
        if (this.isInDisabledWorld(entity.getWorld().getName())) {
            return;
        }

        // Check if the shooter was a player
        if (!(egg.getShooter() instanceof Player)) {
            return;
        }
        // The player, who shot the egg
        Player player = (Player) egg.getShooter();

        // Check if WorldGuard is available
        //TODO: Move to PlayerCaptureMobEvent???
        if (this.getPlugin().isWorldGuardAvailable()) {
            // Wrapped WorldGuard player
            LocalPlayer localPlayer = this.getPlugin().getWorldGuardDependency().getWorldGuardPlugin().wrapPlayer(player);
            // List of regions the entity is currently in.
            ApplicableRegionSet regions = this.getPlugin().getWorldGuardDependency().getWorldGuardPlugin()
                    .getRegionManager(entity.getWorld()).getApplicableRegions(entity.getLocation());
            // Check if entity is in a known region
            if (regions.iterator().hasNext()) {
                // First region the entity is standing in
                ProtectedRegion region = regions.iterator().next();
                MobCapture.logger.info("Region: " + region.getId());
                // Check if player is either a member or an owner of the region. If not cancel event.
                if (!region.getOwners().contains(localPlayer) || region.getMembers().contains(localPlayer)) {
                    Message.sendWithLogo(player, "&cYou aren't allowed to catch mobs here.");
                    // Add thrown egg to storage for PlayerEggThrowEvent
                    this.getPlugin().getEggStorage().add(egg);
                    // Cancel event
                    event.setCancelled(true);
                    return;
                }
            }
            // No explicit region found, so check flag of '__global__' region. Cancel event if denied
            if (!regions.testState(localPlayer, (StateFlag) this.getPlugin().getWorldGuardDependency().getCaptureMobs())) {
                Message.sendWithLogo(player, "&cCaching mobs is disabled here.");
                this.getPlugin().getEggStorage().add(egg);
                event.setCancelled(true);
                return;
            }
        }
        // The lore for our spawn egg.
        List<String> lore = this.buildLore(entity);

        ItemStack mobEgg = new ItemStack(Material.MONSTER_EGG, 1);
        SpawnEggMeta spawnEggMeta = (SpawnEggMeta) mobEgg.getItemMeta();
        spawnEggMeta.setSpawnedType(entity.getType());
        spawnEggMeta.setLore(lore);
        mobEgg.setItemMeta(spawnEggMeta);

        PlayerCaptureMobEvent playerCaptureMobEvent = new PlayerCaptureMobEvent(mobEgg, player, entity);
        Bukkit.getPluginManager().callEvent(playerCaptureMobEvent);

        if (playerCaptureMobEvent.isCancelled()) {
            if (!player.getGameMode().equals(GameMode.CREATIVE))
                player.getInventory().addItem(new ItemStack(Material.EGG, 1));
            this.getPlugin().getEggStorage().add(egg);
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        entity.remove();
        this.playEffectAndSoundIfEnabled(entity);
        this.getPlugin().getEggStorage().add(egg);
        entity.getUniqueId();
        Item item = entity.getWorld().dropItem(entity.getLocation(), playerCaptureMobEvent.getSpawnEggStack());
        //MobCapture.logger.info(item.getUniqueId().toString());
    }

    private void playEffectAndSoundIfEnabled(LivingEntity entity) {
        if (this.getPlugin().getConfig().getBoolean("particle.enable")) {
            this.playEffect(entity);
        }
        if (this.getPlugin().getConfig().getBoolean("sound.enable")) {
            this.playSound(entity);
        }
    }

    private void playSound(LivingEntity entity) {
        entity.getWorld().playSound(
                entity.getLocation(),
                Sound.valueOf(this.getPlugin().getConfig().getString("sound.type")),
                this.getPlugin().getConfig().getInt("sound.volume"),
                this.getPlugin().getConfig().getInt("sound.pitch")
        );
    }

    private void playEffect(LivingEntity entity) {
        entity.getWorld().spawnParticle(
                Particle.valueOf(this.getPlugin().getConfig().getString("particle.type")),
                entity.getLocation(),
                this.getPlugin().getConfig().getInt("particle.amount"),
                this.getPlugin().getConfig().getDouble("particle.xd"),
                this.getPlugin().getConfig().getDouble("particle.yd"),
                this.getPlugin().getConfig().getDouble("particle.zd"),
                this.getPlugin().getConfig().getDouble("particle.velocity")
        );
    }

    private List<String> buildLore(LivingEntity entity) {
        List<String> lore = new ArrayList<>();
        lore.add(Message.replaceColors("&9Mob: &e") + entity.getType().toString());//monsterType.getCreatureType());

        if (entity.getCustomName() != null) {
            lore.add(Message.replaceColors("&9Name: &e" + entity.getCustomName()));
        }

        if (entity instanceof Ocelot) {
            lore.add(Message.replaceColors("&9Cat Type: &e" + ((Ocelot) entity).getCatType()));
        } else if (entity instanceof Horse) {
            lore.add(Message.replaceColors("&9Style: &e" + ((Horse) entity).getStyle()));
            lore.add(Message.replaceColors("&9Horse Color: &e" + ((Horse) entity).getColor()));
        } else if (entity instanceof Villager) {
            lore.add(Message.replaceColors("&9Profession: &e" + ((Villager) entity).getProfession()));
        } else if (entity instanceof Sheep) {
            lore.add(Message.replaceColors("&9Sheared: &e" + ((Sheep) entity).isSheared()));
        } else if (entity instanceof Wolf) {
            lore.add(Message.replaceColors("&9Collar Color: &e" + ((Wolf) entity).getCollarColor()));
            lore.add(Message.replaceColors("&9Angry: &e" + ((Wolf) entity).isAngry()));
        } else if (entity instanceof ZombieVillager) {
            lore.add(Message.replaceColors("&9Profession: &e" + ((ZombieVillager) entity).getVillagerProfession()));
        } else if (entity instanceof Slime) {
            lore.add(Message.replaceColors("&9Size: &e" + ((Slime) entity).getSize()));
        } else if (entity instanceof Llama) {
            lore.add(Message.replaceColors("&9Llama Color: &e" + ((Llama) entity).getColor()));
            lore.add(Message.replaceColors("&9Strength: &e" + ((Llama) entity).getStrength()));
        } else if (entity instanceof Creeper) {
            lore.add(Message.replaceColors("&9Powered: &e" + ((Creeper) entity).isPowered()));
        } else if (entity instanceof Enderman) {
            lore.add(Message.replaceColors("&9Block: &e" + ((Enderman) entity).getCarriedMaterial().getItemType()));
        } else if (entity instanceof Rabbit) {
            lore.add(Message.replaceColors("&9Rabbit Type: &e" + ((Rabbit) entity).getRabbitType()));
        }

        lore.add(Message.replaceColors(String.format(
                "&9Health: &e%s/%s",
                decimalFormat.format(entity.getHealth()),
                decimalFormat.format(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
        )));

        if (entity instanceof Ageable) {
            Ageable ageable = (Ageable) entity;
            if (ageable.isAdult()) {
                lore.add(Message.replaceColors("&9Age: &eAdult"));
            } else {
                lore.add(Message.replaceColors("&9Age: &eBaby"));
            }
        } else if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if (!zombie.isBaby()) {
                lore.add(Message.replaceColors("&9Baby: &efalse"));
            } else {
                lore.add(Message.replaceColors("&9Baby: &etrue"));
            }
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable) entity;
            //TODO: Add color of message based on entity color
            lore.add(Message.replaceColors("&9Color: &e" + colorable.getColor()));
        }

        if (entity instanceof Tameable) {
            //TODO: Cast directly instead of new object
            Tameable tameable = (Tameable) entity;
            lore.add(Message.replaceColors("&9Tamed: &e" + tameable.isTamed()));
            if (tameable.isTamed()) {
                lore.add(Message.replaceColors("&9Owner: &e" + tameable.getOwner().getName()));
                lore.add(Message.replaceColors("&9UUID: &e" + tameable.getOwner().getUniqueId()));
            }
        }

        if (entity instanceof ChestedHorse) {
            lore.add(Message.replaceColors("&9Chest: &e" + ((ChestedHorse) entity).isCarryingChest()));
        }

        if (entity instanceof InventoryHolder && !this.isInventoryEmpty(((InventoryHolder) entity).getInventory())) {
            InventoryHolder inventoryHolder = (InventoryHolder) entity;
            String enc = InventoryUtil.toBase64(inventoryHolder.getInventory());
            String uuid = "";
            try {
                //Files.write(Paths.get(this.plugin.getDataFolder().getAbsolutePath() + File.separator + uuid + ".inv"), enc.getBytes());
                uuid = InventoryUtil.saveInventory(this.getPlugin().getDataFolder().getAbsolutePath(), inventoryHolder.getInventory(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //lore.add(Message.replaceColors("&9Inventory: &e" + uuid));
            lore.add(Message.replaceColors("&9Inventory: &eYes" + InventoryUtil.hideText(uuid)));
        }

        if (this.hasEquipment(entity.getEquipment())) {
            String armor = InventoryUtil.toBase64(entity.getEquipment().getArmorContents());
            String itemMainHand = InventoryUtil.toBase64(entity.getEquipment().getItemInMainHand());
            String itemOffHand = InventoryUtil.toBase64(entity.getEquipment().getItemInOffHand());
            String all = armor + ":" + itemMainHand + ":" + itemOffHand;
            String uuid = "";
            try {
                //Files.write(Paths.get(this.plugin.getDataFolder().getAbsolutePath() + File.separator + uuid + ".eqpm"), all.getBytes());
                uuid = InventoryUtil.saveEqipment(this.getPlugin().getDataFolder().getAbsolutePath(), entity.getEquipment(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //lore.add(Message.replaceColors("&9Equipment: &e" + uuid));
            lore.add(Message.replaceColors("&9Equipment: &eYes" + InventoryUtil.hideText(uuid)));
        }

        return lore;
    }

    private boolean isInventoryEmpty(Inventory inventory) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null) MobCapture.logger.info(itemStack.getType().toString());
            if (itemStack != null) return false;
        }

        return true;
    }

    private boolean hasEquipment(EntityEquipment equipment) {
        if (!equipment.getItemInMainHand().getType().equals(Material.AIR)
                || !equipment.getItemInOffHand().getType().equals(Material.AIR)) {
            return true;
        }

        for (ItemStack itemStack : equipment.getArmorContents()) {
            MobCapture.logger.info(itemStack.getType().toString());
            if (!itemStack.getType().equals(Material.AIR)) return true;
        }

        return false;
    }

    /**
     * Checks if the given world is in the list of disabled worlds.
     *
     * @param world Name of the world to check.
     * @return true if disabled, false if enabled.
     */
    private boolean isInDisabledWorld(String world) {
        return this.getPlugin().getConfig().getList("worlds.disabled").contains(world);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEndermanHitWithEgg(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg)) {
            return;
        }

        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getHitEntity();

        Egg egg = (Egg) event.getEntity();
        MonsterType monsterType = MonsterType.getEggType(entity);

        if (monsterType == null) {
            return;
        }

        if (!(entity instanceof Enderman)) {
            return;
        }

        if (this.getPlugin().getConfig().getList("worlds.disabled").contains(entity.getWorld().getName())) {
            return;
        }

        if (egg.getShooter() instanceof Player) {
            Player player = (Player) egg.getShooter();

            List<String> lore = new ArrayList<>();

            lore.add(Message.replaceColors("&9Mob: &e") + monsterType.getCreatureType());

            lore.add(Message.replaceColors("&9Block: &e" + ((Enderman) entity).getCarriedMaterial().getItemType()));

            if (entity.getCustomName() != null) {
                lore.add(Message.replaceColors("&9Name: &e" + entity.getCustomName()));
            }

            lore.add(Message.replaceColors(String.format(
                    "&9Health: &e%s/%s",
                    decimalFormat.format(entity.getHealth()),
                    decimalFormat.format(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
            )));

            ItemStack mobEgg = new ItemStack(Material.MONSTER_EGG, 1);
            SpawnEggMeta spawnEggMeta = (SpawnEggMeta) mobEgg.getItemMeta();
            spawnEggMeta.setSpawnedType(entity.getType());
            spawnEggMeta.setLore(lore);
            mobEgg.setItemMeta(spawnEggMeta);

            PlayerCaptureMobEvent playerCaptureMobEvent = new PlayerCaptureMobEvent(mobEgg, player, entity);
            Bukkit.getPluginManager().callEvent(playerCaptureMobEvent);

            if (playerCaptureMobEvent.isCancelled()) {
                if (!player.getGameMode().equals(GameMode.CREATIVE))
                    player.getInventory().addItem(new ItemStack(Material.EGG, 1));
                this.getPlugin().getEggStorage().add(egg);
                return;
            }
            entity.remove();
            if (this.getPlugin().getConfig().getBoolean("particle.enable")) {
                entity.getWorld().spawnParticle(
                        Particle.valueOf(this.getPlugin().getConfig().getString("particle.type")),
                        egg.getLocation(),
                        this.getPlugin().getConfig().getInt("particle.amount"),
                        this.getPlugin().getConfig().getDouble("particle.xd"),
                        this.getPlugin().getConfig().getDouble("particle.yd"),
                        this.getPlugin().getConfig().getDouble("particle.zd"),
                        this.getPlugin().getConfig().getDouble("particle.velocity")
                );
            }

            if (this.getPlugin().getConfig().getBoolean("sound.enable")) {
                entity.getWorld().playSound(
                        egg.getLocation(),
                        Sound.valueOf(this.getPlugin().getConfig().getString("sound.type")),
                        this.getPlugin().getConfig().getInt("sound.volume"),
                        this.getPlugin().getConfig().getInt("sound.pitch")
                );
            }
            this.getPlugin().getEggStorage().add(egg);
            entity.getWorld().dropItem(egg.getLocation(), playerCaptureMobEvent.getSpawnEggStack());
        }
    }
}
