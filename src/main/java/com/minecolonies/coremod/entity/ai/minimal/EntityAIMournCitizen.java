package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.Status;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.colony.buildings.modules.GraveyardManagementModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;

/**
 * Citizen mourning goal. Has citizens randomly walk around townhall.
 */
public class EntityAIMournCitizen extends Goal
{
    /**
     * Different mourning states.
     */
    public enum MourningState implements IState
    {
        IDLE,
        DECIDE,
        WALKING_TO_TOWNHALL,
        WANDERING,
        STARING,
        WALKING_TO_GRAVEYARD,
        WANDER_AT_GRAVEYARD,
        WALK_TO_GRAVE
    }

    /**
     * AI statemachine
     */
    private final TickRateStateMachine<MourningState> stateMachine;

    /**
     * handler to the citizen thisis located
     */
    private final EntityCitizen citizen;

    /**
     * Speed at which the citizen will move around
     */
    private final double speed;

    /**
     * Pointer to the closest citizen to look at.
     */
    private Entity closestEntity;

    /**
     * Constant values of mourning
     */
    private static final int MIN_DESTINATION_TO_LOCATION = 225;
    private static final int AVERAGE_MOURN_TIME = 60 * 5;
    private static final int AVERAGE_STARE_TIME = 10 * 20;

    /**
     * The position of the graveyard.
     */
    private BlockPos graveyard;

    /**
     * Position of the grave to walk to.
     */
    private BlockPos gravePos;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAIMournCitizen(final EntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(MourningState.IDLE, e -> Log.getLogger().warn("Mourning AI threw exception:", e));

        stateMachine.addTransition(new TickingTransition<>(MourningState.IDLE, () -> true, () -> MourningState.DECIDE, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.DECIDE, () -> true, this::decide, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WALKING_TO_TOWNHALL, () -> true, this::walkToTownHall, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WANDERING, () -> true, this::wander, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.STARING, () -> true, this::stare, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WALKING_TO_GRAVEYARD, () -> true, this::walkToGraveyard, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WANDER_AT_GRAVEYARD, () -> true, this::wanderAtGraveyard, 20));
        stateMachine.addTransition(new TickingTransition<>(MourningState.WALK_TO_GRAVE, () -> true, this::walkToGrave, 20));
    }

    /**
     * Check if the citizen should still be mourning.
     * @return true if so.
     */
    private boolean shouldMourn()
    {
        final boolean shouldMourn = citizen.getDesiredActivity() == DesiredActivity.MOURN;
        if (shouldMourn && this.citizen.getRandom().nextInt(AVERAGE_MOURN_TIME) < 1)
        {
            this.citizen.getCitizenData().getCitizenMournHandler().clearDeceasedCitizen();
            this.citizen.getCitizenData().getCitizenMournHandler().setMourning(false);
            citizen.getCitizenData().setVisibleStatus(null);
        }
        return shouldMourn;
    }

    /**
     * Path to the townhall.
     * @return IDLE again.
     */
    private MourningState walkToTownHall()
    {
        final BlockPos pos = getMournLocation();
        citizen.getNavigation().moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), this.speed);
        return MourningState.IDLE;
    }

    /**
     * Path to the graveyard.
     * @return the next state to go to.
     */
    private MourningState walkToGraveyard()
    {
        if (graveyard == null)
        {
            return MourningState.DECIDE;
        }

        if (!citizen.isWorkerAtSiteWithMove(graveyard, 3))
        {
            return MourningState.WALKING_TO_GRAVEYARD;
        }

        return MourningState.WANDER_AT_GRAVEYARD;
    }

    /**
     * Wander at the graveyard and visit the graves.
     * While wandering at the graveyard, the chance to stop mourning is doubled.
     * @return the next state to go to.
     */
    private MourningState wanderAtGraveyard()
    {
        if (graveyard == null)
        {
            return MourningState.DECIDE;
        }

        // Double the chance to stop mourning by checking here as well.
        if (!shouldMourn())
        {
            return MourningState.IDLE;
        }

        if (!(citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBuilding(graveyard) instanceof final BuildingGraveyard graveyardBuilding))
        {
            graveyard = null;
            return MourningState.DECIDE;
        }

        if (!citizen.getNavigation().isDone())
        {
            return MourningState.WANDER_AT_GRAVEYARD;
        }

        // Wander around randomly.
        if (MathUtils.RANDOM.nextInt(100) < 90)
        {
            citizen.getNavigation().moveToRandomPos(10, DEFAULT_SPEED, graveyardBuilding.getCorners(), AbstractAdvancedPathNavigate.RestrictionType.XYZ);
            return MourningState.WANDER_AT_GRAVEYARD;
        }

        // Try find the grave of one of the diseased.
        for (final Tuple<BlockPos, Direction> gravePos : graveyardBuilding.getGravePositions())
        {
            if (WorldUtil.isBlockLoaded(citizen.level, gravePos.getA()))
            {
                if (citizen.level.getBlockEntity(gravePos.getA()) instanceof final TileEntityNamedGrave namedGrave)
                {
                    final Iterator<String> iterator = citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens().iterator();
                    if (!iterator.hasNext())
                    {
                        continue;
                    }
                    final String deathBud = iterator.next();
                    final String firstName = StringUtils.split(deathBud)[0];
                    final String lastName = deathBud.replaceFirst(firstName,"");

                    final List<String> graveNameList = namedGrave.getTextLines();
                    if (!graveNameList.isEmpty() && graveNameList.contains(firstName) && graveNameList.contains(lastName))
                    {
                        this.gravePos = gravePos.getA();
                        return MourningState.WALK_TO_GRAVE;
                    }
                }
            }
        }

        return MourningState.DECIDE;
    }

    /**
     * Walk to grave state.
     * @return next state.
     */
    private MourningState walkToGrave()
    {
        if (gravePos == null)
        {
            return MourningState.DECIDE;
        }

        // Double the chance to stop mourning by checking here as well.
        if (!shouldMourn())
        {
            return MourningState.IDLE;
        }

        if (!citizen.isWorkerAtSiteWithMove(gravePos, 3))
        {
            return MourningState.WALK_TO_GRAVE;
        }

        return MourningState.DECIDE;
    }

    /**
     * Wander around randomly.
     * @return also IDLE again.
     */
    private MourningState wander()
    {
        citizen.getNavigation().moveToRandomPos(10, this.speed);
        return MourningState.IDLE;
    }

    /**
     * State at a random player around.
     * @return Staring if there is a player, else IDLE.
     */
    private MourningState stare()
    {
        if (this.citizen.getRandom().nextInt(AVERAGE_STARE_TIME) < 1)
        {
            closestEntity = null;
            return MourningState.IDLE;
        }

        if (closestEntity == null)
        {
            closestEntity = this.citizen.level.getNearestEntity(EntityCitizen.class,
              TargetingConditions.DEFAULT,
              citizen,
              citizen.getX(),
              citizen.getY(),
              citizen.getZ(),
              citizen.getBoundingBox().inflate(3.0D, 3.0D, 3.0D));

            if (closestEntity == null)
            {
                return MourningState.IDLE;
            }
        }

        citizen.getLookControl().setLookAt(closestEntity.getX(), closestEntity.getY() + (double) closestEntity.getEyeHeight(), closestEntity.getZ(), (float) citizen.getMaxHeadYRot(), (float) citizen.getMaxHeadXRot());
        return MourningState.STARING;
    }

    /**
     * Decide what to do next.
     * @return the next state to go to.
     */
    private MourningState decide()
    {
        if (citizen.getDesiredActivity() != DesiredActivity.MOURN)
        {
            return MourningState.IDLE;
        }

        if (!citizen.getNavigation().isDone())
        {
            return MourningState.IDLE;
        }

        if (citizen.getCitizenStatusHandler().getStatus() != Status.MOURN)
        {
            citizen.getCitizenItemHandler().removeHeldItem();
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.MOURNING);
            citizen.getCitizenStatusHandler().setStatus(Status.MOURN);
        }

        if (this.citizen.getRandom().nextBoolean())
        {
            return MourningState.STARING;
        }

        if (this.graveyard == null)
        {
            this.graveyard = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getFirstBuildingMatching(b -> b instanceof BuildingGraveyard && b.getFirstModuleOccurance(
              GraveyardManagementModule.class).hasRestingCitizen(citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens()));
        }

        if (graveyard != null)
        {
            return MourningState.WALKING_TO_GRAVEYARD;
        }

        citizen.getLookControl().setLookAt(citizen.getX(), citizen.getY() - 10, citizen.getZ(), (float) citizen.getMaxHeadYRot(),
          (float) citizen.getMaxHeadXRot());

        if (BlockPosUtil.getDistance2D(this.citizen.blockPosition(), getMournLocation()) > MIN_DESTINATION_TO_LOCATION)
        {
            return MourningState.WALKING_TO_TOWNHALL;
        }

        return MourningState.WANDERING;
    }

    @Override
    public boolean canUse()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.MOURN && MathUtils.RANDOM.nextInt(20) < 1)
        {
            return shouldMourn();
        }
        return citizen.getDesiredActivity() == DesiredActivity.MOURN;
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    @Override
    public void stop()
    {
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
        this.graveyard = null;
        this.gravePos = null;
    }

    /**
     * Call this function to get the mourn location
     *
     * @return blockPos of the location to mourn at
     */
    protected BlockPos getMournLocation()
    {
        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        if (colony == null || !colony.getBuildingManager().hasTownHall())
        {
            return citizen.getRestrictCenter();
        }

        return colony.getBuildingManager().getTownHall().getPosition();
    }
}
