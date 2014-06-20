package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EnumStatus;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAISleep extends EntityAIBase
{
    private EntityCitizen citizen;

    public EntityAISleep(EntityCitizen citizen)
    {
        this.setMutexBits(1);
        this.citizen = citizen;
    }

    @Override
    public boolean shouldExecute()
    {
        return !citizen.worldObj.isDaytime() && isHome();//!this.citizen.isWorkTime? - sleep when raining?
    }

    private boolean isHome()
    {
        return this.citizen.getHomeHut() != null && this.citizen.getHomeHut().getDistanceFrom(citizen.posX, citizen.posY, citizen.posZ) < 4;
    }

    @Override
    public void startExecuting()
    {
        //TODO sleep
        citizen.setStatus(EnumStatus.SLEEPING);
    }

    @Override
    public void updateTask()
    {

    }


}
