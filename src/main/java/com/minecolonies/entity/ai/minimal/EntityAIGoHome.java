package com.minecolonies.entity.ai.minimal;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Log;
import com.minecolonies.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
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
     * The citizen.
     */
    private EntityCitizen citizen;

    /**
     * Chance to play goHomeSound.
     */
    private static final int CHANCE = 100;

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
            Log.getLogger().error("EntityCitizen has null townHall (And no home)");
            return;
        }

        playGoHomeSounds();

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

        citizen.isWorkerAtSiteWithMove(pos, 2);
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void playGoHomeSounds()
    {
        final int chance = citizen.getRandom().nextInt(CHANCE);

        if(chance <= 1)
        {
            if (citizen.getWorkBuilding() != null && citizen.getColonyJob() != null)
            {
                SoundUtils.playSoundAtCitizenWithChance(citizen.worldObj, citizen.getPosition(), citizen.getColonyJob().getBedTimeSound(), 1);
            }
            //add further workers as soon as available.
        }
    }
}
