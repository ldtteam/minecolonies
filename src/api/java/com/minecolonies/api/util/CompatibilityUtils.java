package com.minecolonies.api.util;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * This class group method use to insure compatibility between minecraft version.
 * <p>
 * This is the 1.15 version
 */
public final class CompatibilityUtils
{
    private CompatibilityUtils()
    {
        //hide the constructor
    }

    /**
     * get the world from the entity.
     * <p>
     * This method is aiming to hide the difference between 1.10.2 and 1.11.2
     *
     * @param entity to which we get the world from
     * @return the world
     */
    public static World getWorldFromEntity(final Entity entity)
    {
        return entity.level;
    }

    public static World getWorldFromCitizen(final AbstractEntityCitizen baseEntityCitizen)
    {
        return getWorldFromEntity(baseEntityCitizen);
    }

    /**
     * spawn an entity in the world
     * <p>
     * This method is aiming to hide the difference between 1.10.2 and 1.11.2
     *
     * @param world         The world which we are spawning the entity in.
     * @param entityToSpawn The entity which we are spawning.
     */
    public static void addEntity(final World world, final Entity entityToSpawn)
    {
        world.addFreshEntity(entityToSpawn);
    }
}

