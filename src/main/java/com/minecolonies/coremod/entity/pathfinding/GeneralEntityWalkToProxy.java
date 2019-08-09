package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.pathfinding.AbstractWalkToProxy;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
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
    public GeneralEntityWalkToProxy(final EntityLiving entity)
    {
        super(entity);
    }

    @Override
    public Set<BlockPos> getWayPoints()
    {
        final EntityLiving living = getEntity();

        final IColony colony = IColonyManager.getInstance().getClosestColony(living.getEntityWorld(), living.getPosition());

        if (colony == null || !colony.isCoordInColony(living.getEntityWorld(), living.getPosition()))
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
