package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

/**
 * EntityCitizen go home AI.
 * Created: May 25, 2014
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
    private final EntityCitizen citizen;

    /**
     * Constructor for the task, creates task.
     *
     * @param citizen the citizen to assign to this task.
     */
    public EntityAIGoHome(EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
    }

    /**
     * Checks if the task should be executed.
     * Only try to go home if he should sleep and he isn't home already.
     *
     * @return true if should execute.
     */
    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP
                 && !citizen.isAtHome();
    }

    /**
     * Only execute if the citizen has no path atm (meaning while he isn't pathing at the moment)
     *
     * @return true if he should continue.
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath() && citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP;
    }

    @Override
    public void startExecuting()
    {
        final BlockPos pos = citizen.getHomePosition();
        if (pos == null)
        {
            //If the citizen has no colony as well, remove the citizen.
            if (citizen.getColony() == null)
            {
                citizen.onDeath(CLEANUP_DAMAGE);
            }
            else
            {
                //If he has no homePosition strangely then try to  move to the colony.
                citizen.isWorkerAtSiteWithMove(citizen.getColony().getCenter(), 2);
            }
            return;
        }

        playGoHomeSounds();

        citizen.isWorkerAtSiteWithMove(pos, 2);
    }

    @Override
    public void setMutexBits(final int mutexBitsIn)
    {
        super.setMutexBits(1);
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void playGoHomeSounds()
    {
        final int chance = citizen.getRandom().nextInt(CHANCE);

        if (chance <= 1 && citizen.getWorkBuilding() != null && citizen.getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(citizen.world, citizen.getPosition(), citizen.getColonyJob().getBedTimeSound(), 1);
            //add further workers as soon as available.
        }
    }
}
