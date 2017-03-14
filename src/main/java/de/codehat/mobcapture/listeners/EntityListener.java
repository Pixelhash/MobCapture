package de.codehat.mobcapture.listeners;

import de.codehat.mobcapture.MobCapture;
import de.codehat.mobcapture.events.PlayerCaptureMobEvent;
import de.codehat.mobcapture.util.Message;
import de.codehat.mobcapture.util.MonsterType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.Colorable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EntityListener implements Listener {

    private MobCapture plugin;
    private DecimalFormat decimalFormat = new DecimalFormat("#.0");

    public EntityListener(MobCapture plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageWithEgg(EntityDamageEvent event) {

        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();

        EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;

        if (!(damageByEntityEvent.getDamager() instanceof Egg)) {
            return;
        }

        Egg egg = (Egg) damageByEntityEvent.getDamager();
        MonsterType monsterType = MonsterType.getEggType(entity);

        if (monsterType == null) {
            return;
        }

        if (this.plugin.getConfig().getList("worlds.disabled").contains(entity.getWorld().getName())) {
            return;
        }

        if (egg.getShooter() instanceof Player) {
            Player player = (Player) egg.getShooter();

            List<String> lore = new ArrayList<>();

            lore.add(Message.replaceColors("&9Mob: &e") + monsterType.getCreatureType());

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

            if(entity.getCustomName() != null) {
                lore.add(Message.replaceColors("&9Name: &e" + entity.getCustomName()));
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

            ItemStack mobEgg = new ItemStack(Material.MONSTER_EGG, 1);
            SpawnEggMeta spawnEggMeta = (SpawnEggMeta) mobEgg.getItemMeta();
            spawnEggMeta.setSpawnedType(entity.getType());
            spawnEggMeta.setLore(lore);
            mobEgg.setItemMeta(spawnEggMeta);

            PlayerCaptureMobEvent playerCaptureMobEvent = new PlayerCaptureMobEvent(mobEgg, player, entity);
            Bukkit.getPluginManager().callEvent(playerCaptureMobEvent);

            if (playerCaptureMobEvent.isCancelled()) {
                if (!player.getGameMode().equals(GameMode.CREATIVE)) player.getInventory().addItem(new ItemStack(Material.EGG, 1));
                this.plugin.getEggStorage().add(egg);
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            entity.remove();
            if (this.plugin.getConfig().getBoolean("particle.enable")) {
                entity.getWorld().spawnParticle(
                        Particle.valueOf(this.plugin.getConfig().getString("particle.type")),
                        entity.getLocation(),
                        this.plugin.getConfig().getInt("particle.amount"),
                        this.plugin.getConfig().getDouble("particle.xd"),
                        this.plugin.getConfig().getDouble("particle.yd"),
                        this.plugin.getConfig().getDouble("particle.zd"),
                        this.plugin.getConfig().getDouble("particle.velocity")
                );
            }

            if (this.plugin.getConfig().getBoolean("sound.enable")) {
                entity.getWorld().playSound(
                        entity.getLocation(),
                        Sound.valueOf(this.plugin.getConfig().getString("sound.type")),
                        this.plugin.getConfig().getInt("sound.volume"),
                        this.plugin.getConfig().getInt("sound.pitch")
                );
            }
            this.plugin.getEggStorage().add(egg);
            entity.getWorld().dropItem(entity.getLocation(), playerCaptureMobEvent.getSpawnEggStack());
        }


    }

}
