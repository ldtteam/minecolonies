package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobWalkRandomEdge;
import org.jetbrains.annotations.NotNull;

/**
 * Druid AI class, which deals with equipment and movement specifics
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIDruid extends AbstractEntityAIGuard<JobDruid, AbstractBuildingGuards>
{
    public EntityAIDruid(@NotNull final JobDruid job)
    {
        super(job);
        new RangerCombatAI((EntityCitizen) worker, getStateAI(), this);
    }
    
    @Override
    public void guardMovement()
    {
        if (worker.getRandom().nextInt(3) < 1)
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 3);
            return;
        }

        if (worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 10) || Math.abs(buildingGuards.getGuardPos().getY() - worker.blockPosition().getY()) > 3)
        {
            // Moves the druid randomly to close edges, for better vision to mobs
            ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(new PathJobWalkRandomEdge(world, buildingGuards.getGuardPos(), 20, worker),
              null,
              1.0, true);
        }
    }
}
