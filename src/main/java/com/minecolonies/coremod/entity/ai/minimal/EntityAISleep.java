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

import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
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
    private BlockPos usedBed = null;

    /**
     * Check if the citizen woke up already.
     */
    private boolean wokeUp = true;

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
        return (citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP && citizen.isAtHome()) || !wokeUp;
    }

    /**
     * Continue executing if he should sleep.
     * Call the wake up method as soon as this isn't the case anymore.
     * Might search a bed while he is trying to sleep.
     *
     * @return true while he should sleep.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        if (citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP)
        {
            wokeUp = false;
            final Colony colony = citizen.getColony();
            if (colony == null || colony.getBuildingManager().getBuilding(citizen.getHomePosition()) == null)
            {
                return true;
            }

            if (usedBed == null)
            {
                final AbstractBuilding hut = colony.getBuildingManager().getBuilding(citizen.getHomePosition());
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

                            final BlockPos feetPos = pos.offset(state.getValue(BlockBed.FACING).getOpposite());
                            final IBlockState feetState = citizen.world.getBlockState(feetPos);
                            citizen.world.setBlockState(feetPos, feetState.withProperty(BlockBed.OCCUPIED, true), 0x03);

                            return true;
                        }
                    }
                }

                usedBed = citizen.getHomePosition();
            }
            else
            {
                if (citizen.isWorkerAtSiteWithMove(usedBed, 1))
                {
                    citizen.trySleep(usedBed);
                    return true;
                }
            }
            return true;
        }

        citizen.onWakeUp();
        if (usedBed != null)
        {
            final IBlockState state = citizen.world.getBlockState(usedBed);
            if (state.getBlock() instanceof BlockBed)
            {
                final IBlockState headState = citizen.world.getBlockState(usedBed);
                citizen.world.setBlockState(usedBed, headState.withProperty(BlockBed.OCCUPIED, false), 0x03);

                final BlockPos feetPos = usedBed.offset(headState.getValue(BlockBed.FACING).getOpposite());
                final IBlockState feetState = citizen.world.getBlockState(feetPos);
                citizen.world.setBlockState(feetPos, feetState.withProperty(BlockBed.OCCUPIED, true), 0x03);
            }
            usedBed = null;

        }
        wokeUp = true;
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
