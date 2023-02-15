package com.minecolonies.api.entity.pathfinding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Path;
import java.util.concurrent.Callable;

public interface IAbstractPathJob extends Callable<Path>
{
    /**
     * Sync the path of a given mob to the client.
     *
     * @param mob the tracked mob.
     */
    void synchToClient(final LivingEntity mob);
}
