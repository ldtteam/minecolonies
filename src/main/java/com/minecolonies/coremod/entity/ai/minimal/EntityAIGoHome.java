package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

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
     * Filter to allow citizen requesting without spam.
     */
    @NotNull
    protected final ChatSpamFilter chatSpamFilter;

    /**
     * The citizen.
     */
    private final EntityCitizen citizen;

    /**
     * Constructor for the task, creates task.
     *
     * @param citizen the citizen to assign to this task.
     */
    public EntityAIGoHome(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.chatSpamFilter = new ChatSpamFilter(citizen.getCitizenData());
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
        return !citizen.getCitizenColonyHandler().isAtHome()  && (citizen.getDesiredActivity() == DesiredActivity.SLEEP);
    }


    /**
     * Only execute if the citizen has no path atm (meaning while he isn't pathing at the moment)
     *
     * @return true if he should continue.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !citizen.getNavigator().noPath() && (citizen.getDesiredActivity() == DesiredActivity.SLEEP);
    }

    @Override
    public void startExecuting()
    {
        final BlockPos pos = citizen.getHomePosition();
        if (pos == null || pos.equals(BlockPos.ORIGIN))
        {
            //If the citizen has no colony as well, remove the citizen.
            if (citizen.getCitizenColonyHandler().getColony() == null)
            {
                citizen.onDeath(CLEANUP_DAMAGE);
            }
            else
            {
                //If he has no homePosition strangely then try to  move to the colony.
                citizen.isWorkerAtSiteWithMove(citizen.getCitizenColonyHandler().getColony().getCenter(), 2);
            }
            return;
        }
        else
        {
            citizen.isWorkerAtSiteWithMove(pos, 2);
        }
        
        playGoHomeSounds();
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

        if (chance <= 1 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getPosition(), citizen.getCitizenJobHandler().getColonyJob().getBedTimeSound(), 1);
            //add further workers as soon as available.
        }
    }
}
