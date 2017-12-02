package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.coremod.entity.EntityCitizen.Status.SLEEPING;

/**
 * AI to send Entity to sleep.
 */
public class EntityAISleep extends EntityAIBase
{
    /**
     * The citizen.
     */
    private final EntityCitizen citizen;

    /**
     * Bed the citizen is using atm.
     */
    private BlockPos usedBed;

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
        usedBed = citizen.getHomePosition();
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
            if(usedBed == null)
            {
                return true;
            }

            final Colony colony = citizen.getColony();
            if(colony == null || colony.getBuilding(citizen.getHomePosition()) == null)
            {
                return true;
            }

            if(usedBed.equals(citizen.getHomePosition()))
            {
                final AbstractBuilding hut = colony.getBuilding(citizen.getHomePosition());
                if (hut instanceof BuildingHome)
                {
                    for (final BlockPos pos : ((BuildingHome) hut).getBedList())
                    {
                        final World world = citizen.world;
                        IBlockState state = world.getBlockState(pos);
                        state = state.getBlock().getActualState(state, world, pos);
                        if (state.getBlock() instanceof BlockBed
                                && !state.getValue(BlockBed.OCCUPIED)
                                && state.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.HEAD))
                        {
                            usedBed = pos;
                            citizen.world.setBlockState(pos, state.withProperty(BlockBed.OCCUPIED, true), 0x03);
                            return true;
                        }
                    }
                }

                usedBed = citizen.getHomePosition();
            }

            citizen.isWorkerAtSiteWithMove(usedBed, 1);
            return true;
        }

        citizen.onWakeUp();
        if(usedBed != null)
        {
            final IBlockState state = citizen.world.getBlockState(usedBed);
            if(state.getBlock() instanceof BlockBed)
            {
                citizen.world.setBlockState(usedBed, state.withProperty(BlockBed.OCCUPIED, false), 0x03);
            }
            usedBed = null;
        }

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
