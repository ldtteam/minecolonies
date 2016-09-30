package com.minecolonies.entity.ai.minimal;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.sounds.CitizenSounds;
import com.minecolonies.sounds.FishermanSounds;
import com.minecolonies.util.Log;
import com.minecolonies.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;

import java.util.Random;

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
    private static final int ONE_IN_A_THOUSAND = 1000;

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

        citizen.getNavigator().tryMoveToXYZ((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, 1.0D);
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void playGoHomeSounds()
    {
        Random rand = new Random();

        int chance = rand.nextInt(ONE_IN_A_THOUSAND);

        if(chance <= 1)
        {
            if (citizen.getWorkBuilding() != null && ("fisherman").equals(citizen.getWorkBuilding().getJobName()))
            {
                SoundUtils.playSoundAtCitizenWithChance(citizen.worldObj, citizen.getPosition(), FishermanSounds.Female.offToBed, 1);
            }
            //todo add for other workers as soon as available.
        }
    }
}
