package com.minecolonies.entity.ai.minimal;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;

import static com.minecolonies.entity.EntityCitizen.Status.SLEEPING;

/**
 * AI to send Entity to sleep
 */
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
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP &&
               citizen.isAtHome();
    }

    @Override
    public boolean continueExecuting()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP;
    }

    @Override
    public void startExecuting()
    {
        //TODO sleep
        citizen.setStatus(SLEEPING);
    }

    @Override
    public void updateTask()
    {
        //TODO snore?
    }
}
