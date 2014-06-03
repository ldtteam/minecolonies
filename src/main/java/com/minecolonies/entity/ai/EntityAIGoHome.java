package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;

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
        return !citizen.worldObj.isDaytime() || citizen.worldObj.isRaining();
    }

    @Override
    public void startExecuting()
    {
        Vec3 vec;
        if(citizen.getHomeHut() != null)
        {
            vec = citizen.getHomeHut().getPosition();
        }
        else if(citizen.getTownHall() != null)
        {
            vec = citizen.getTownHall().getPosition();
        }
        else
        {
            MineColonies.logger.error("EntityCitizen has null townhall (And no home)");
            return;
        }
        citizen.getNavigator().tryMoveToXYZ(vec.xCoord, vec.yCoord, vec.zCoord, 1.0F);
    }

    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath();
    }
}
