package com.minecolonies.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * This class group method use to insure compatibility between minecraft version.
 *
 * This is the 1.10 version
 */
public final class CompatibilityUtils
{
    private CompatibilityUtils()
    {
        //hide the constructor
    }

    /**
     * get the world from the entity.
     *
     * This method is aiming to hide the difference between 1.10.2 and 1.11.2
     * @param entity to which we get the wolrd from
     * @return the world
     */
    public static World getWorld(final Entity entity)
    {
        return entity.worldObj;
    }

}
