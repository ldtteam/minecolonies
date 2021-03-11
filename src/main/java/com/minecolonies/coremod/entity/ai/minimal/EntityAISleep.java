package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.BedHandlingModule;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.SleepingParticleMessage;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAISleep.SleepState.*;

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
     * Ticks of putting the citizen into bed.
     */
    private int bedTicks = 0;

    public enum SleepState implements IState
    {
        AWAKE,
        WALKING_HOME,
        FIND_BED,
        SLEEPING;
    }

    /**
     * The AI's state machine
     */
    private TickRateStateMachine<SleepState> stateMachine;

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
        // 100 blocks - 30 seconds - straight line
        stateMachine = new TickRateStateMachine<>(SleepState.AWAKE, e -> Log.getLogger().warn(e));
        stateMachine.addTransition(new TickingTransition<>(AWAKE, this::wantSleep, () -> {
            initAI();
            return WALKING_HOME;
        }, 20));

        stateMachine.addTransition(new TickingTransition<>(WALKING_HOME, () -> true, this::walkHome, 30));
        stateMachine.addTransition(new TickingTransition<>(FIND_BED, () -> !wantSleep(), () -> {
            resetAI();
            return AWAKE;
        }, 20));
        stateMachine.addTransition(new TickingTransition<>(FIND_BED, this::findBed, () -> SLEEPING, 30));

        stateMachine.addTransition(new TickingTransition<>(SLEEPING, () -> !wantSleep(), () -> {
            resetAI();
            return AWAKE;
        }, 20));
        stateMachine.addTransition(new TickingTransition<>(SLEEPING, () -> true, this::sleep, TICK_INTERVAL));
    }

    /**
     * Walking to the home/bed position
     *
     * @return
     */
    private SleepState walkHome()
    {
        final IBuilding homeBuilding = citizen.getCitizenData().getHomeBuilding();
        if (homeBuilding == null)
        {
            @Nullable final BlockPos homePosition = citizen.getHomePosition();
            if (homePosition.distanceSq(Math.floor(citizen.getPosX()), citizen.getPosY(), Math.floor(citizen.getPosZ()), false) <= RANGE_TO_BE_HOME)
            {
                return FIND_BED;
            }
        }
        else if (homeBuilding.isInBuilding(citizen.getPositionVec()))
        {
            return FIND_BED;
        }

        citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SLEEP);
        goHome();
        return WALKING_HOME;
    }

    /**
     * Tries to find a fitting bed, or timeouts
     *
     * @return true if continue to sleep
     */
    private boolean findBed()
    {
        if (!citizen.getCitizenSleepHandler().isAsleep() || bedTicks < MAX_BED_TICKS)
        {
            findBedAndTryToSleep();
            return false;
        }
        return true;
    }

    /**
     * Whether the citizen wants to sleep
     *
     * @return true if wants to sleep
     */
    private boolean wantSleep()
    {
        return citizen.getRevengeTarget() == null && (citizen.getDesiredActivity() == DesiredActivity.SLEEP);
    }

    /**
     * Tests if the sleeping should be executed. Only execute if he should sleep and he is at home.
     *
     * @return true if so.
     */
    @Override
    public boolean shouldExecute()
    {
        stateMachine.tick();
        return stateMachine.getState() != AWAKE;
    }

    /**
     * Continue executing if he should sleep. Call the wake up method as soon as this isn't the case anymore. Might search a bed while he is trying to sleep.
     *
     * @return true while he should sleep.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return stateMachine.getState() != AWAKE && wantSleep();
    }

    /**
     * On init set his status to sleeping.
     */
    public void initAI()
    {
        citizen.getCitizenStatusHandler().setStatus(Status.SLEEPING);
        usedBed = null;
    }

    /**
     * Called while he is trying to sleep. Might add sleeping sounds here.
     */
    @Override
    public void tick()
    {
        stateMachine.tick();
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

        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony != null && colony.getBuildingManager().getBuilding(citizen.getHomePosition()) != null)
        {
            if (usedBed == null)
            {
                final IBuilding hut = colony.getBuildingManager().getBuilding(citizen.getHomePosition());
                List<BlockPos> bedList = new ArrayList<>();
                if (hut.hasModule(BedHandlingModule.class))
                {
                    hut.getFirstModuleOccurance(BedHandlingModule.class).ifPresent(module -> bedList.addAll(module.getRegisteredBlocks()));
                }

                for (final BlockPos pos : bedList)
                {
                    if (WorldUtil.isEntityBlockLoaded(citizen.world, pos))
                    {
                        final World world = citizen.world;
                        final BlockState state = world.getBlockState(pos);
                        if (state.getBlock().isIn(BlockTags.BEDS)
                              && !state.get(BedBlock.OCCUPIED)
                              && state.get(BedBlock.PART).equals(BedPart.HEAD)
                              && !isBedOccupied(hut, pos)
                              && !world.getBlockState(pos.up()).getMaterial().isSolid())
                        {
                            usedBed = pos;
                            setBedOccupied(true);
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
    private SleepState sleep()
    {
        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(citizen.getPosX(), citizen.getPosY() + 1.0d, citizen.getPosZ()), citizen);
        //TODO make sleeping noises here.
        return null;
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void goHome()
    {
        final BlockPos pos = citizen.getCitizenSleepHandler().findHomePos();
        citizen.isWorkerAtSiteWithMove(pos, 2);

        final int chance = citizen.getRandom().nextInt(CHANCE);
        if (chance <= 1 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getPosition(), EventType.OFF_TO_BED, citizen.getCitizenData());
            //add further workers as soon as available.
        }
    }

    /**
     * Sets the used beds occupied state.
     *
     * @param occupied whether the bed should be occupied.
     */
    private void setBedOccupied(boolean occupied)
    {
        final BlockState headState = citizen.world.getBlockState(usedBed);
        citizen.world.setBlockState(usedBed, headState.with(BedBlock.OCCUPIED, occupied), 0x03);

        final BlockPos feetPos = usedBed.offset(headState.get(BedBlock.HORIZONTAL_FACING).getOpposite());
        final BlockState feetState = citizen.world.getBlockState(feetPos);

        if (feetState.getBlock().isIn(BlockTags.BEDS))
        {
            citizen.world.setBlockState(feetPos, feetState.with(BedBlock.OCCUPIED, occupied), 0x03);
        }
    }

    /**
     * Checks whether any of the citizens living in this hut are sleeping in the given bed.
     *
     * @param hut the hut this citizen is living in.
     * @param bed the bed to check.
     * @return whether any of the citizens living in this hut are sleeping in the given bed.
     */
    private boolean isBedOccupied(IBuilding hut, BlockPos bed)
    {
        for (ICitizenData citizen : hut.getAssignedCitizen())
        {
            if (this.citizen.getCivilianID() != citizen.getId())
            {
                if (citizen.getBedPos().equals(bed))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void resetTask()
    {
        resetAI();
        stateMachine.reset();
    }

    /**
     * Resets the AI state
     */
    private void resetAI()
    {
        citizen.getCitizenData().setVisibleStatus(null);
        citizen.getCitizenSleepHandler().onWakeUp();

        // Clean bed state
        if (usedBed != null)
        {
            final BlockState state = citizen.world.getBlockState(usedBed);
            if (state.getBlock().isIn(BlockTags.BEDS))
            {
                final IColony colony = citizen.getCitizenColonyHandler().getColony();
                if (colony != null && colony.getBuildingManager().getBuilding(citizen.getHomePosition()) != null)
                {
                    final IBuilding hut = colony.getBuildingManager().getBuilding(citizen.getHomePosition());
                    if (hut.hasModule(BedHandlingModule.class))
                    {
                        setBedOccupied(false);
                    }
                }
            }
            usedBed = null;
        }

        bedTicks = 0;
    }
}
