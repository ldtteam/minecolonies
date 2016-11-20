package com.minecolonies.entity.ai.minimal;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

/**
 * EntityCitizen go home AI
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIGoHome extends EntityAIBase
{
    /**
     * Chance to play goHomeSound.
     */
    private static final int CHANCE = 100;

    /**
     * Damage source if has to kill citizen.
     */
    private static final DamageSource CLEANUP_DAMAGE = new DamageSource("CleanUpTask");

    /**
     * The citizen.
     */
    private EntityCitizen citizen;

    /**
     * Constructor for the task, creates task.
     * @param citizen the citizen to assign to this task.
     */
    public EntityAIGoHome(EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    @Override
    public void setMutexBits(final int mutexBitsIn)
    {
        super.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP &&
                 !citizen.isAtHome();
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
            //If he has no homePosition strangely then try to  move to the colony.
            if(citizen.getColony() != null)
            {
                citizen.isWorkerAtSiteWithMove(citizen.getColony().getCenter(), 2);
            }
            else
            {
                //If the citizen has no colony as well, remove the citizen.
                citizen.onDeath(CLEANUP_DAMAGE);
            }
            return;
        }

        playGoHomeSounds();

        citizen.isWorkerAtSiteWithMove(pos, 2);
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void playGoHomeSounds()
    {
        final int chance = citizen.getRandom().nextInt(CHANCE);

        if (chance <= 1)
        {
            if (citizen.getWorkBuilding() != null && citizen.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(citizen.worldObj, citizen.getPosition(), citizen.getColonyJob().getBedTimeSound(), 1);
            }
            //add further workers as soon as available.
        }
    }
}
