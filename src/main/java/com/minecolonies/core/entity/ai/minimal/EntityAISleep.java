package com.minecolonies.core.entity.ai.minimal;

import com.ldtteam.domumornamentum.block.decorative.PanelBlock;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.SleepingParticleMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.RANGE_TO_BE_HOME;
import static com.minecolonies.api.util.constant.HappinessConstants.SLEPTTONIGHT;
import static com.minecolonies.core.entity.ai.minimal.EntityAISleep.SleepState.*;

/**
 * AI to send Entity to sleep.
 */
public class EntityAISleep implements IStateAI
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
        WALKING_HOME,
        FIND_BED,
        SLEEPING;
    }

    /**
     * Initiate the sleep task.
     *
     * @param citizen the citizen which should sleep.
     */
    public EntityAISleep(final EntityCitizen citizen)
    {
        this.citizen = citizen;
        // 100 blocks - 30 seconds - straight line
        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.SLEEP, () -> true, this::checkSleep, 20));

        citizen.getCitizenAI().addTransition(new TickingTransition<>(WALKING_HOME, () -> true, this::walkHome, 30));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(FIND_BED, this::findBed, () -> SLEEPING, 30));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(SLEEPING, () -> true, this::sleep, TICK_INTERVAL));
    }

    /**
     * Checks for sleep
     *
     * @return
     */
    private IState checkSleep()
    {
        initAI();
        return WALKING_HOME;
    }

    /**
     * Walking to the home/bed position
     *
     * @return
     */
    private IState walkHome()
    {
        final IBuilding homeBuilding = citizen.getCitizenData().getHomeBuilding();
        if (homeBuilding == null)
        {
            @Nullable final BlockPos homePosition = citizen.getCitizenData().getHomePosition();
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
     * On init set his status to sleeping.
     */
    public void initAI()
    {
        usedBed = null;
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

        final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        if (colony != null && citizen.getCitizenData().getHomeBuilding() instanceof AbstractBuilding hut)
        {
            final BlockPos homePos = citizen.getCitizenData().getHomePosition();
            if (usedBed == null)
            {
                List<BlockPos> bedList = new ArrayList<>();
                if (hut.hasModule(BuildingModules.BED))
                {
                    bedList.addAll(hut.getModule(BuildingModules.BED).getRegisteredBlocks());
                }

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

                usedBed = homePos;
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
    private IState sleep()
    {
        if (usedBed != null)
        {
            if (usedBed.distSqr(citizen.blockPosition()) > 3 * 3)
            {
                return WALKING_HOME;
            }
            citizen.setPose(Pose.SLEEPING);
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
        final BlockPos pos = citizen.getCitizenData().getHomePosition();
        if (pos != null && !citizen.isWorkerAtSiteWithMove(pos, 2) && citizen.getPose() == Pose.SLEEPING)
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
}
