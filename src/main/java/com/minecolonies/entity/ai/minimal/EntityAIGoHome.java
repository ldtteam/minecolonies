package com.minecolonies.entity.ai.minimal;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;

/**
 * EntityCitizen go home AI
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIGoHome extends EntityAIBase
{
    private EntityCitizen citizen;

    public EntityAIGoHome(EntityCitizen citizen)
    {
        setMutexBits(1);
        this.citizen = citizen;
    }

    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP &&
                 !citizen.isAtHome() &&
                 citizen.getNavigator().noPath();
    }

    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }

    @Override
    public void startExecuting()
    {
        BlockPos pos = citizen.getHomePosition();
        if (pos == null)
        {
            MineColonies.getLogger().error("EntityCitizen has null townHall (And no home)");
            return;
        }

        if (citizen.getWorkBuilding() != null)
        {
            /*
            Temp fix for pathfinding in the night.
            Citizens can't find a path home.
             */
            final BlockPos workBuilding = citizen.getWorkBuilding().getLocation();
            citizen.isWorkerAtSiteWithMove(workBuilding, 2);
            return;
        }

        citizen.getNavigator().tryMoveToXYZ((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, 1.0D);
    }
}
