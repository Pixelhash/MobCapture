package de.codehat.mobcapture.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerSpawnCapturedMobEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    @Deprecated
    private ItemStack spawnEggStack;
    private EntityType entityType;
    private Player player;
    private Location location;

    public PlayerSpawnCapturedMobEvent(ItemStack spawnEggStack, EntityType entityType, Player player, Location location) {
        this.spawnEggStack = spawnEggStack;
        this.entityType = entityType;
        this.player = player;
        this.location = location;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location getLocation() {
        return location;
    }

    @Deprecated
    public ItemStack getSpawnEggStack() {
        return this.spawnEggStack;
    }

    @Deprecated
    public void setSpawnEggStack(ItemStack spawnEggStack) {
        this.spawnEggStack = spawnEggStack;
    }
}
