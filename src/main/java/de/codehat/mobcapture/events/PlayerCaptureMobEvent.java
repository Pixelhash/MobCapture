package de.codehat.mobcapture.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerCaptureMobEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    @Deprecated
    private ItemStack spawnEggStack;
    private Player player;
    private LivingEntity entity;

    public PlayerCaptureMobEvent(ItemStack spawnEggStack, Player player, LivingEntity entity) {
        this.spawnEggStack = spawnEggStack;
        this.player = player;
        this.entity = entity;
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

    public Player getPlayer() {
        return this.player;
    }

    public LivingEntity getEntity() {
        return this.entity;
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
