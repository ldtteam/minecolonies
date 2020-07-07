package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A colony event which spawns and uses entities
 */
public interface IColonyEntitySpawnEvent extends IColonySpawnEvent
{
    /**
     * The list of entities related to this event
     *
     * @return the list.
     */
    default List<Entity> getEntities()
    {
        return new ArrayList<>();
    }

    /**
     * Called to register an entity with this event
     *
     * @param entity the entity to register.
     */
    default void registerEntity(final Entity entity) {}

    /**
     * called to unregister an entity with this event
     *
     * @param entity the entity to unregister.
     */
    default void unregisterEntity(final Entity entity) {}

    /**
     * Trigger on entity death.
     *
     * @param entity the killed entity.
     */
    default void onEntityDeath(final LivingEntity entity) {}
}
