package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.SleepingParticleMessage;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * AI to send Entity to sleep.
 */
public class EntityAISleep extends Goal
{
    /**
     * Interval between sleeping particles
     */
    private static final int TICK_INTERVAL = 30;

    /**
     * Chance to play goHomeSound.
     */
    private static final int CHANCE = 33;

    /**
     * Damage source if has to kill citizen.
     */
    private static final DamageSource CLEANUP_DAMAGE = new DamageSource("CleanUpTask");

    /**
     * Max ticks of putting the citizen to bed.
     */
    private static final int MAX_BED_TICKS = 10;

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
     * Timer for emitting sleeping particle effect
     */
    private int tickTimer = 0;

    /**
     * Ticks of putting the citizen into bed.
     */
    private int bedTicks = 0;

    /**
     * Initiate the sleep task.
     *
     * @param citizen the citizen which should sleep.
     */
    public EntityAISleep(final EntityCitizen citizen)
    {
        super();
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
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
        return citizen.getDesiredActivity() == DesiredActivity.SLEEP || !wokeUp;
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
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP)
        {
            return true;
        }

        citizen.getCitizenSleepHandler().onWakeUp();
        if (usedBed != null)
        {
            final BlockState state = citizen.world.getBlockState(usedBed);
            if (state.getBlock().isIn(BlockTags.BEDS))
            {
                final BlockState headState = citizen.world.getBlockState(usedBed);
                citizen.world.setBlockState(usedBed, headState.with(BedBlock.OCCUPIED, false), 0x03);

                final BlockPos feetPos = usedBed.offset(headState.get(BedBlock.HORIZONTAL_FACING).getOpposite());
                final BlockState feetState = citizen.world.getBlockState(feetPos);

                if (feetState.getBlock().isIn(BlockTags.BEDS))
                {
                    citizen.world.setBlockState(feetPos, feetState.with(BedBlock.OCCUPIED, false), 0x03);
                }
            }
            usedBed = null;
        }
        wokeUp = true;
        bedTicks = 0;
        return false;
    }

    /**
     * On start executing set his status to sleeping.
     */
    @Override
    public void startExecuting()
    {
        citizen.getCitizenStatusHandler().setStatus(Status.SLEEPING);
    }

    /**
     * Called while he is trying to sleep.
     * Might add sleeping sounds here.
     */
    @Override
    public void tick()
    {
        tickTimer++;
        if (tickTimer % TICK_INTERVAL != 0)
        {
            return;
        }
        tickTimer = 0;

        // Go home
        if (!citizen.getCitizenColonyHandler().isAtHome())
        {
            goHome();
            return;
        }

        if (!citizen.getCitizenSleepHandler().isAsleep() || bedTicks < MAX_BED_TICKS)
        {
            findBedAndTryToSleep();
        }
        else
        {
            // Do the actual sleeping action.
            sleep();
        }
    }

    private void findBedAndTryToSleep()
    {
        // Finding bed
        if (usedBed == null && citizen.getCitizenData() != null)
        {
            this.usedBed = citizen.getCitizenData().getBedPos();
            if (citizen.getCitizenData().getBedPos().equals(BlockPos.ZERO))
            {
                this.usedBed = null;
            }
        }

        this.wokeUp = false;
        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony != null && colony.getBuildingManager().getBuilding(citizen.getHomePosition()) != null)
        {
            if (usedBed == null)
            {
                final IBuilding hut = colony.getBuildingManager().getBuilding(citizen.getHomePosition());
                if (hut instanceof BuildingHome)
                {
                    for (final BlockPos pos : ((BuildingHome) hut).getBedList())
                    {
                        final World world = citizen.world;
                        BlockState state = world.getBlockState(pos);
                        state = state.getBlock().getExtendedState(state, world, pos);
                        if (state.getBlock().isIn(BlockTags.BEDS)
                              && !state.get(BedBlock.OCCUPIED)
                              && state.get(BedBlock.PART).equals(BedPart.HEAD)
                              && world.isAirBlock(pos.up()))
                        {
                            usedBed = pos;
                            citizen.world.setBlockState(pos, state.with(BedBlock.OCCUPIED, true), 0x03);

                            final BlockPos feetPos = pos.offset(state.get(BedBlock.HORIZONTAL_FACING).getOpposite());
                            final BlockState feetState = citizen.world.getBlockState(feetPos);
                            if (feetState.getBlock().isIn(BlockTags.BEDS))
                            {
                                citizen.world.setBlockState(feetPos, feetState.with(BedBlock.OCCUPIED, true), 0x03);
                            }
                            return;
                        }
                    }
                }
                usedBed = citizen.getHomePosition();
            }

            if (citizen.isWorkerAtSiteWithMove(usedBed, 3))
            {
                bedTicks++;
                if (!citizen.getCitizenSleepHandler().trySleep(usedBed))
                {
                    citizen.getCitizenData().setBedPos(BlockPos.ZERO);
                    usedBed = null;
                }
                citizen.getCitizenData().getCitizenHappinessHandler().resetModifier("slepttonight");
            }
        }
    }

    /**
     * Make sleeping
     */
    private void sleep()
    {
        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(citizen.posX, citizen.posY + 1.0d, citizen.posZ), citizen);
        //TODO make sleeping noises here.
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void goHome()
    {
        final BlockPos pos = citizen.getHomePosition();
        if (pos == null || pos.equals(BlockPos.ZERO))
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

        final int chance = citizen.getRandom().nextInt(CHANCE);

        if (chance <= 1 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorldFromCitizen(citizen),
              citizen.getPosition(),
              citizen.getCitizenJobHandler().getColonyJob().getBedTimeSound(),
              1);
            //add further workers as soon as available.
        }
    }
}
