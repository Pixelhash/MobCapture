package de.codehat.mobcapture.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public enum MonsterType {
    EVOKER(EntityType.EVOKER),
    VEX(EntityType.VEX),
    VINDICATOR(EntityType.VINDICATOR),
    PIG_ZOMBIE(EntityType.PIG_ZOMBIE),
    MAGMA_CUBE(EntityType.MAGMA_CUBE),
    CAVE_SPIDER(EntityType.CAVE_SPIDER),
    MUSHROOM_COW(EntityType.MUSHROOM_COW),
    CREEPER(EntityType.CREEPER),
    WITHER_SKELETON(EntityType.WITHER_SKELETON),
    STRAY(EntityType.STRAY),
    SKELETON(EntityType.SKELETON),
    SPIDER(EntityType.SPIDER),
    HUSK(EntityType.HUSK),
    ZOMBIE_VILLAGER(EntityType.ZOMBIE_VILLAGER),
    ZOMBIE(EntityType.ZOMBIE),
    SLIME(EntityType.SLIME),
    GHAST(EntityType.GHAST),
    ENDERMAN(EntityType.ENDERMAN),
    SILVERFISH(EntityType.SILVERFISH),
    BLAZE(EntityType.BLAZE),
    PIG(EntityType.PIG),
    SHEEP(EntityType.SHEEP),
    COW(EntityType.COW),
    CHICKEN(EntityType.CHICKEN),
    SQUID(EntityType.SQUID),
    WOLF(EntityType.WOLF),
    VILLAGER(EntityType.VILLAGER),
    OCELOT(EntityType.OCELOT),
    BAT(EntityType.BAT),
    WITCH(EntityType.WITCH),
    ZOMBIE_HORSE(EntityType.ZOMBIE_HORSE),
    SKELETON_HORSE(EntityType.SKELETON_HORSE),
    LLAMA(EntityType.LLAMA),
    DONKEY(EntityType.DONKEY),
    MULE(EntityType.MULE),
    HORSE(EntityType.HORSE),
    ENDERMITE(EntityType.ENDERMITE),
    ELDER_GUARDIAN(EntityType.ELDER_GUARDIAN),
    GUARDIAN(EntityType.GUARDIAN),
    RABBIT(EntityType.RABBIT),
    POLAR_BEAR(EntityType.POLAR_BEAR),
    SHULKER(EntityType.SHULKER);

    private final EntityType entityType;

    MonsterType(EntityType entityType) {
        this.entityType = entityType;
    }

    public static MonsterType getEggType(Entity entity) {
        for (MonsterType eggType : MonsterType.values()) {
            if (!eggType.getCreatureType().getEntityClass().isInstance(entity)) {
                continue;
            }
            return eggType;
        }
        return null;
    }

    public EntityType getCreatureType() {
        return this.entityType;
    }
}
