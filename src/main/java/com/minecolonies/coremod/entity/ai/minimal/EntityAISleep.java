package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.entity.ai.EntityAIBase;

import static com.minecolonies.coremod.entity.EntityCitizen.Status.SLEEPING;

/**
 * AI to send Entity to sleep.
 */
public class EntityAISleep extends EntityAIBase
{
    private final EntityCitizen citizen;

    /**
     * Initiate the sleep task.
     *
     * @param citizen the citizen which should sleep.
     */
    public EntityAISleep(final EntityCitizen citizen)
    {
        super();
        this.setMutexBits(1);
        this.citizen = citizen;
    }

    /**
     * Tests if the sleeping should be executed.
     * Only execute if he should sleep and he is at home.
     *
     * @return true if so.
     */
    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP
                 && citizen.isAtHome();
    }

    /**
     * Continue executing if he should sleep.
     * Call the wake up method as soon as this isn't the case anymore.
     * Might search a bed while he is trying to sleep.
     *
     * @return true while he should sleep.
     */
    @Override
    public boolean continueExecuting()
    {
        if (citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP)
        {
            //TODO might search a bed?
            return true;
        }

        citizen.onWakeUp();

        return false;
    }

    /**
     * On start executing set his status to sleeping.
     */
    @Override
    public void startExecuting()
    {
        citizen.setStatus(SLEEPING);
    }

    /**
     * Called while he is trying to sleep.
     * Might add sleeping sounds here.
     */
    @Override
    public void updateTask()
    {
        //TODO make sleeping noises here.
    }
}
