package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ChunkCoordinates;

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
        return !citizen.isWorkTime();
    }

    @Override
    public void startExecuting()
    {
        ChunkCoordinates pos;
        if(citizen.getHomeHut() != null)
        {
            pos = citizen.getHomeHut().getPosition();
        }
        else if(citizen.getTownHall() != null)
        {
            pos = citizen.getTownHall().getPosition();
        }
        else
        {
            MineColonies.logger.error("EntityCitizen has null townhall (And no home)");
            return;
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(citizen, pos);
    }

    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }
}
