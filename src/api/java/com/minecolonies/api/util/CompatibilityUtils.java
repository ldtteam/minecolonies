package com.minecolonies.api.util;

import com.minecolonies.coremod.entity.IBaseEntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * This class group method use to insure compatibility between minecraft version.
 * <p>
 * This is the 1.14 version
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
     * @param entity to which we get the wolrd from
     * @return the world
     */
    public static World getWorldFromEntity(final Entity entity)
    {
        return entity.world;
    }

    public static World getWorldFromCitizen(final IBaseEntityCitizen baseEntityCitizen)
    {
        return getWorldFromEntity((Entity) baseEntityCitizen);
    }

    /**
     * spawn an entity in the world
     * <p>
     * This method is aiming to hide the differnece between 1.10.2 and 1.11.2
     *
     * @param world         The world which we are spawning the entity in.
     * @param entityToSpawn The entity which we are spawning.
     */
    public static void addEntity(final World world, final Entity entityToSpawn)
    {
        world.addEntity(entityToSpawn);
    }
}
