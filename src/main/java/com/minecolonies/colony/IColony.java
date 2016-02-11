package com.minecolonies.colony;

import com.minecolonies.colony.permissions.IPermissions;
import net.minecraft.world.World;

public interface IColony
{
    String getName();

    IPermissions getPermissions();

    boolean isCoordInColony(World w, int x, int y, int z);

    float getDistanceSquared(int x, int y, int z);

    boolean hasTownhall();
}
