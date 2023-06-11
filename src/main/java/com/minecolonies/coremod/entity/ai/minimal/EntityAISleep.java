package com.minecolonies.coremod.entity.ai.minimal;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
import static com.minecolonies.api.util.constant.HappinessConstants.SLEPTTONIGHT;
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
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.citizen = citizen;
        // 100 blocks - 30 seconds - straight line
        stateMachine = new TickRateStateMachine<>(SleepState.AWAKE, e -> Log.getLogger().warn(e));
        stateMachine.addTransition(new TickingTransition<>(AWAKE, () -> true, this::checkSleep, 20));

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
     * Checks for sleep
     * @return
     */
    private SleepState checkSleep()
    {
        if (wantSleep())
        {
            initAI();
            return WALKING_HOME;
        }

        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            citizen.getCitizenSleepHandler().onWakeUp();
        }

        if (citizen.getPose() == Pose.SLEEPING)
        {
            citizen.setPose(Pose.STANDING);
        }

        return AWAKE;
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
            @Nullable final BlockPos homePosition = citizen.getRestrictCenter();
            if (homePosition.distSqr(BlockPos.containing(Math.floor(citizen.getX()), citizen.getY(), Math.floor(citizen.getZ()))) <= RANGE_TO_BE_HOME)
            {
                return FIND_BED;
            }
        }
        else if (homeBuilding.isInBuilding(citizen.blockPosition()))
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
        return citizen.getLastHurtByMob() == null && (citizen.getDesiredActivity() == DesiredActivity.SLEEP);
    }

    /**
     * Tests if the sleeping should be executed. Only execute if he should sleep and he is at home.
     *
     * @return true if so.
     */
    @Override
    public boolean canUse()
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
    public boolean canContinueToUse()
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
        if (colony != null && colony.getBuildingManager().getBuilding(citizen.getRestrictCenter()) != null)
        {
            if (usedBed == null)
            {
                final IBuilding hut = colony.getBuildingManager().getBuilding(citizen.getRestrictCenter());
                List<BlockPos> bedList = new ArrayList<>();
                hut.getFirstOptionalModuleOccurance(BedHandlingModule.class).ifPresent(module -> bedList.addAll(module.getRegisteredBlocks()));

                for (final BlockPos pos : bedList)
                {
                    if (WorldUtil.isEntityBlockLoaded(citizen.level, pos))
                    {
                        final Level world = citizen.level;
                        final BlockState state = world.getBlockState(pos);
                        final BlockState above = world.getBlockState(pos.above());
                        if (state.is(BlockTags.BEDS)
                              && !state.getValue(BedBlock.OCCUPIED)
                              && state.getValue(BedBlock.PART).equals(BedPart.HEAD)
                              && !isBedOccupied(hut, pos)
                              && (above.is(BlockTags.BEDS) || above.getBlock() instanceof PanelBlock || above.getBlock() instanceof TrapDoorBlock || !above.isSolid()))
                        {
                            usedBed = pos;
                            setBedOccupied(true);
                            return;
                        }
                    }
                }

                usedBed = citizen.getRestrictCenter();
            }

            if (citizen.isWorkerAtSiteWithMove(usedBed, 3))
            {
                bedTicks++;
                if (!citizen.getCitizenSleepHandler().trySleep(usedBed))
                {
                    citizen.getCitizenData().setBedPos(BlockPos.ZERO);
                    usedBed = null;
                }
                citizen.getCitizenData().getCitizenHappinessHandler().resetModifier(SLEPTTONIGHT);
            }
        }
    }

    /**
     * Make sleeping
     */
    private SleepState sleep()
    {
        if (usedBed != null && usedBed.distSqr(citizen.blockPosition()) > 3 * 3)
        {
            return WALKING_HOME;
        }

        Network.getNetwork().sendToTrackingEntity(new SleepingParticleMessage(citizen.getX(), citizen.getY() + 1.0d, citizen.getZ()), citizen);
        //TODO make sleeping noises here.
        return null;
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void goHome()
    {
        final BlockPos pos = citizen.getCitizenSleepHandler().findHomePos();
        if (!citizen.isWorkerAtSiteWithMove(pos, 2) && citizen.getPose() == Pose.SLEEPING)
        {
            citizen.setPose(Pose.STANDING);
        }

        final int chance = citizen.getRandom().nextInt(CHANCE);
        if (chance <= 1 && citizen.getCitizenColonyHandler().getWorkBuilding() != null && citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWith(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.blockPosition(), EventType.OFF_TO_BED, citizen.getCitizenData());
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
        final BlockState headState = citizen.level.getBlockState(usedBed);
        citizen.level.setBlock(usedBed, headState.setValue(BedBlock.OCCUPIED, occupied), 0x03);

        final BlockPos feetPos = usedBed.relative(headState.getValue(BedBlock.FACING).getOpposite());
        final BlockState feetState = citizen.level.getBlockState(feetPos);

        if (feetState.is(BlockTags.BEDS))
        {
            citizen.level.setBlock(feetPos, feetState.setValue(BedBlock.OCCUPIED, occupied), 0x03);
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
        for (ICitizenData citizen : hut.getAllAssignedCitizen())
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
    public void stop()
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
            final BlockState state = citizen.level.getBlockState(usedBed);
            if (state.is(BlockTags.BEDS))
            {
                final IColony colony = citizen.getCitizenColonyHandler().getColony();
                if (colony != null && colony.getBuildingManager().getBuilding(citizen.getRestrictCenter()) != null)
                {
                    final IBuilding hut = colony.getBuildingManager().getBuilding(citizen.getRestrictCenter());
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
