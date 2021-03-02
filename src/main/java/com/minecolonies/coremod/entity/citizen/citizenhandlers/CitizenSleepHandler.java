package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSleepHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.colony.interactionhandling.SimpleNotificationInteraction;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Pose;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_BED_POS;
import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_IS_ASLEEP;
import static com.minecolonies.api.research.util.ResearchConstants.WORK_LONGER;
import static com.minecolonies.api.util.constant.CitizenConstants.NIGHT;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Handles the sleep of the citizen.
 */
public class CitizenSleepHandler implements ICitizenSleepHandler
{
    /**
     * The additional weight for Y diff
     */
    private static final double Y_DIFF_WEIGHT = 1.5;

    /**
     * The rough time traveling one block takes, in ticks
     */
    private static final double TIME_PER_BLOCK           = 6;
    private static final double MAX_NO_COMPLAIN_DISTANCE = 160;

    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenSleepHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Is the citizen a sleep?
     *
     * @return true when a sleep.
     */
    @Override
    public boolean isAsleep()
    {
        return citizen.getDataManager().get(DATA_IS_ASLEEP);
    }

    /**
     * Sets if the citizen is a sleep. Caution: Use trySleep(BlockPos) for better control
     *
     * @param isAsleep True to make the citizen sleep.
     */
    private void setIsAsleep(final boolean isAsleep)
    {
        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setAsleep(isAsleep);
        }
        citizen.getDataManager().set(DATA_IS_ASLEEP, isAsleep);
    }

    /**
     * Returns the orientation of the bed in degrees.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public float getBedOrientationInDegrees()
    {
        final BlockState state = getBedLocation() == null ? null : citizen.world.getBlockState(getBedLocation());
        if (state != null && state.getBlock().isBed(state, citizen.world, getBedLocation(), citizen))
        {
            switch (state.getBlock().getBedDirection(state, citizen.world, getBedLocation()))
            {
                case SOUTH:
                    return NINETY_DEGREE;
                case WEST:
                    return 0.0F;
                case NORTH:
                    return THREE_QUARTERS;
                case EAST:
                    return HALF_ROTATION;
                default:
                    return 0F;
            }
        }

        return 0.0F;
    }

    /**
     * Attempts a sleep interaction with the citizen and the given bed.
     *
     * @param bedLocation The possible location to sleep.
     */
    @Override
    public boolean trySleep(final BlockPos bedLocation)
    {
        final BlockState state = WorldUtil.isEntityBlockLoaded(citizen.world, bedLocation) ? citizen.world.getBlockState(bedLocation) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, citizen.world, bedLocation, citizen);

        if (!isBed)
        {
            return false;
        }

        citizen.updatePose(Pose.SLEEPING);
        citizen.getNavigator().clearPath();
        citizen.setPosition(((float) bedLocation.getX() + HALF_BLOCK),
          (float) bedLocation.getY() + 0.8F,
          ((float) bedLocation.getZ() + HALF_BLOCK));
        citizen.setBedPosition(bedLocation);

        citizen.setMotion(Vector3d.ZERO);
        citizen.isAirBorne = true;

        //Remove item while citizen is asleep.
        citizen.getCitizenItemHandler().removeHeldItem();

        setIsAsleep(true);


        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setBedPos(bedLocation);
        }
        citizen.getDataManager().set(DATA_BED_POS, bedLocation);

        citizen.getCitizenData().getColony().getCitizenManager().onCitizenSleep();

        return true;
    }

    /**
     * Called when the citizen wakes up.
     */
    @Override
    public void onWakeUp()
    {
        notifyCitizenHandlersOfWakeUp();

        //Only do this if he really sleeps
        if (!isAsleep())
        {
            return;
        }
        citizen.updatePose(Pose.STANDING);
        citizen.clearBedPosition();
        spawnCitizenFromBed();
    }

    private void notifyCitizenHandlersOfWakeUp()
    {
        if (citizen.getCitizenColonyHandler().getWorkBuilding() != null)
        {
            citizen.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.working"));
            citizen.getCitizenColonyHandler().getWorkBuilding().onWakeUp();
        }
        if (citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            citizen.getCitizenJobHandler().getColonyJob().onWakeUp();
        }

        final IBuilding homeBuilding = citizen.getCitizenColonyHandler().getHomeBuilding();
        if (homeBuilding != null)
        {
            homeBuilding.onWakeUp();
        }
    }

    private void spawnCitizenFromBed()
    {
        final BlockPos spawn;
        if (!getBedLocation().equals(BlockPos.ZERO) && citizen.world.getBlockState(getBedLocation()).getBlock().isIn(BlockTags.BEDS))
        {
            final Optional<Vector3d> spawnVec = Blocks.RED_BED.getBedSpawnPosition(ModEntities.CITIZEN, citizen.world.getBlockState(getBedLocation()), citizen.world, getBedLocation(), 0, citizen);
            spawn = spawnVec.map(BlockPos::new).orElseGet(() -> getBedLocation().up());
        }
        else
        {
            spawn = citizen.getPosition();
        }

        if (spawn != null && !spawn.equals(BlockPos.ZERO))
        {
            citizen.setPosition(spawn.getX() + HALF_BLOCK, spawn.getY() + HALF_BLOCK, spawn.getZ() + HALF_BLOCK);
        }

        setIsAsleep(false);
        if (citizen.getCitizenData() != null)
        {
            citizen.getCitizenData().setBedPos(new BlockPos(0, 0, 0));
        }
        citizen.getDataManager().set(DATA_BED_POS, new BlockPos(0, 0, 0));
    }

    @Override
    public BlockPos findHomePos()
    {
        final BlockPos pos = citizen.getHomePosition();
        if (pos.equals(BlockPos.ZERO))
        {
            if (citizen.getCitizenColonyHandler().getColony().hasTownHall())
            {
                return citizen.getCitizenColonyHandler().getColony().getBuildingManager().getTownHall().getPosition();
            }

            return citizen.getCitizenColonyHandler().getColony().getCenter();
        }

        return pos;
    }

    /**
     * Get the bed location of the citizen.
     *
     * @return the bed location.
     */
    @Override
    public BlockPos getBedLocation()
    {
        return citizen.getDataManager().get(DATA_BED_POS);
    }

    /**
     * Get the X render offset.
     *
     * @return the offset.
     */
    @Override
    public float getRenderOffsetX()
    {
        if (!isAsleep())
        {
            return 0;
        }

        final BlockState state = WorldUtil.isEntityBlockLoaded(citizen.world, getBedLocation()) ? citizen.world.getBlockState(getBedLocation()) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, citizen.world, getBedLocation(), citizen);
        final Direction Direction = isBed && state.getBlock() instanceof HorizontalBlock ? state.get(HorizontalBlock.HORIZONTAL_FACING) : null;

        if (Direction == null)
        {
            return 0;
        }

        return SLEEPING_RENDER_OFFSET * (float) Direction.getXOffset();
    }

    /**
     * Get the z render offset.
     *
     * @return the offset.
     */
    @Override
    public float getRenderOffsetZ()
    {
        if (!isAsleep())
        {
            return 0;
        }

        final BlockState state = WorldUtil.isEntityBlockLoaded(citizen.world, getBedLocation()) ? citizen.world.getBlockState(getBedLocation()) : null;
        final boolean isBed = state != null && state.getBlock().isBed(state, citizen.world, getBedLocation(), citizen);
        final Direction Direction = isBed && state.getBlock() instanceof HorizontalBlock ? state.get(HorizontalBlock.HORIZONTAL_FACING) : null;

        if (Direction == null)
        {
            return 0;
        }

        return SLEEPING_RENDER_OFFSET * (float) Direction.getZOffset();
    }

    @Override
    public boolean shouldGoSleep()
    {
        final BlockPos homePos = findHomePos();
        BlockPos citizenPos = citizen.getPosition();

        int additionalDist = 0;

        // Additional distance for miners
        if (citizen.getCitizenData().getJob() instanceof JobMiner && citizen.getCitizenData().getWorkBuilding().getPosition().getY() - 20 > citizenPos.getY())
        {
            final BlockPos workPos = citizen.getCitizenData().getWorkBuilding().getID();
            additionalDist = (int) BlockPosUtil.getDistance2D(citizenPos, workPos) + Math.abs(citizenPos.getY() - workPos.getY()) * 3;
            citizenPos = workPos;
        }

        // Calc distance with some y weight
        final int xDiff = Math.abs(homePos.getX() - citizenPos.getX());
        final int zDiff = Math.abs(homePos.getZ() - citizenPos.getZ());
        final int yDiff = (int) (Math.abs(homePos.getY() - citizenPos.getY()) * Y_DIFF_WEIGHT);

        final double timeNeeded = (Math.sqrt(xDiff * xDiff + zDiff * zDiff + yDiff * yDiff) + additionalDist) * TIME_PER_BLOCK;

        // Estimated arrival is 1hour past night
        final double timeLeft = (citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(WORK_LONGER) > 0
                                   ? NIGHT : NIGHT + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(WORK_LONGER) * 1000) - (citizen.world.getDayTime() % 24000);
        if (timeLeft <= 0 || (timeLeft - timeNeeded <= 0))
        {
            if (citizen.getCitizenData().getWorkBuilding() != null)
            {
                final double workHomeDistance = Math.sqrt(BlockPosUtil.getDistanceSquared(homePos, citizen.getCitizenData().getWorkBuilding().getID()));
                if (workHomeDistance > MAX_NO_COMPLAIN_DISTANCE)
                {
                    citizen.getCitizenData()
                      .triggerInteraction(new SimpleNotificationInteraction(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.hometoofar"), ChatPriority.IMPORTANT));
                }
            }
            return true;
        }

        return false;
    }
}
