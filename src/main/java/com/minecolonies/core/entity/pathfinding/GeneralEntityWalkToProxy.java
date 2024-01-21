package com.minecolonies.core.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.pathfinding.proxy.AbstractWalkToProxy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * General walkToProxy for all entities.
 */
public class GeneralEntityWalkToProxy extends AbstractWalkToProxy
{

    /**
     * Creates a walkToProxy for a certain worker.
     *
     * @param entity the entity.
     */
    public GeneralEntityWalkToProxy(final Mob entity)
    {
        super(entity);
    }

    @Override
    public Set<BlockPos> getWayPoints()
    {
        final LivingEntity living = getEntity();
        final BlockPos pos = living.blockPosition();
        final IColony colony = IColonyManager.getInstance().getClosestColony(living.getCommandSenderWorld(), pos);

        if (colony == null || !colony.isCoordInColony(living.getCommandSenderWorld(), pos))
        {
            return Collections.emptySet();
        }

        return colony.getWayPoints().keySet();
    }

    @Override
    public boolean careAboutY()
    {
        return false;
    }

    @Nullable
    @Override
    public BlockPos getSpecializedProxy(final BlockPos target, final double distanceToPath)
    {
        return null;
    }
}
