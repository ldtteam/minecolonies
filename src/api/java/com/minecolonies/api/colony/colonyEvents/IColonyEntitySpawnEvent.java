package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

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
     * @return
     */
    default public List<Entity> getEntities()
    {
        return new ArrayList<>();
    }

    /**
     * Called to register an entity with this event
     *
     * @param entity
     */
    default public void registerEntity(final Entity entity) {}

    /**
     * called to unregister an entity with this event
     *
     * @param entity
     */
    default public void unregisterEntity(final Entity entity) {}

    /**
     * Trigger on entity death.
     *
     * @param entity
     */
    default public void onEntityDeath(final EntityLiving entity) {}
}
