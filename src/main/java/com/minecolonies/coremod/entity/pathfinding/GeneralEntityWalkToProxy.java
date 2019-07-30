package com.minecolonies.coremod.entity.pathfinding;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ai.pathfinding.AbstractWalkToProxy;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import net.minecraft.entity.LivingEntity;
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
    public GeneralEntityWalkToProxy(final LivingEntity entity)
    {
        super(entity);
    }

    @Override
    public Set<BlockPos> getWayPoints()
    {
        final LivingEntity living = getEntity();

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
