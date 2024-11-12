package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.core.tileentities.TileEntityNamedGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.buildings.modules.GraveyardManagementModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;

/**
 * Citizen mourning goal. Has citizens randomly walk around townhall.
 */
public class EntityAIMournCitizen implements IStateAI
{
    /**
     * Different mourning states.
     */
    public enum MourningState implements IState
    {
        DECIDE,
        WALKING_TO_TOWNHALL,
        WANDERING,
        STARING,
        WALKING_TO_GRAVEYARD,
        WANDER_AT_GRAVEYARD,
        WALK_TO_GRAVE
    }

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
    private static final int AVERAGE_MOURN_TIME          = 60 * 5;
    private static final int AVERAGE_STARE_TIME          = 10 * 20;

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

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.MOURN, () -> {
            reset();
            return true;
        }, () -> MourningState.DECIDE, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.DECIDE, () -> true, this::decide, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.WALKING_TO_TOWNHALL, () -> true, this::walkToTownHall, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.WANDERING, () -> true, this::wander, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.STARING, () -> true, this::stare, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.WALKING_TO_GRAVEYARD, () -> true, this::walkToGraveyard, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.WANDER_AT_GRAVEYARD, () -> true, this::wanderAtGraveyard, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(MourningState.WALK_TO_GRAVE, () -> true, this::walkToGrave, 20));
    }

    /**
     * Path to the townhall.
     *
     * @return IDLE again.
     */
    private IState walkToTownHall()
    {
        final BlockPos pos = getMournLocation();
        if (pos == null)
        {
            return CitizenAIState.IDLE;
        }

        if (!citizen.isWorkerAtSiteWithMove(pos, 3))
        {
            return MourningState.WALKING_TO_TOWNHALL;
        }

        return CitizenAIState.IDLE;
    }

    /**
     * Path to the graveyard.
     *
     * @return the next state to go to.
     */
    private IState walkToGraveyard()
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
     *
     * @return the next state to go to.
     */
    private IState wanderAtGraveyard()
    {
        if (graveyard == null)
        {
            return MourningState.DECIDE;
        }

        final IBuilding graveyardBuilding = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuilding(graveyard);
        if (!(graveyardBuilding instanceof BuildingGraveyard))
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
            citizen.getNavigation().moveToRandomPos(10, DEFAULT_SPEED, graveyardBuilding.getCorners());
            return MourningState.WANDER_AT_GRAVEYARD;
        }

        // Try find the grave of one of the diseased.
        final Set<Tuple<BlockPos, Direction>> gravePositions = ((BuildingGraveyard) graveyardBuilding).getGravePositions();
        for (final Tuple<BlockPos, Direction> gravePos : gravePositions)
        {
            if (WorldUtil.isBlockLoaded(citizen.level, gravePos.getA()))
            {
                final BlockEntity blockEntity = citizen.level.getBlockEntity(gravePos.getA());
                if (blockEntity instanceof TileEntityNamedGrave)
                {
                    final Iterator<String> iterator = citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens().iterator();
                    if (!iterator.hasNext())
                    {
                        continue;
                    }
                    final String deathBud = iterator.next();
                    final String firstName = StringUtils.split(deathBud)[0];
                    final String lastName = deathBud.replaceFirst(firstName, "");

                    final List<String> graveNameList = ((TileEntityNamedGrave) blockEntity).getTextLines();
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
     *
     * @return next state.
     */
    private IState walkToGrave()
    {
        if (gravePos == null)
        {
            return MourningState.DECIDE;
        }

        if (!citizen.isWorkerAtSiteWithMove(gravePos, 3))
        {
            return MourningState.WALK_TO_GRAVE;
        }

        return MourningState.DECIDE;
    }

    /**
     * Wander around randomly.
     *
     * @return also IDLE again.
     */
    private IState wander()
    {
        citizen.getNavigation().moveToRandomPos(10, this.speed);
        return CitizenAIState.IDLE;
    }

    /**
     * State at a random player around.
     *
     * @return Staring if there is a player, else IDLE.
     */
    private IState stare()
    {
        if (this.citizen.getRandom().nextInt(AVERAGE_STARE_TIME) < 1)
        {
            closestEntity = null;
            return CitizenAIState.IDLE;
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
                return CitizenAIState.IDLE;
            }
        }

        citizen.getLookControl()
          .setLookAt(closestEntity.getX(),
            closestEntity.getY() + (double) closestEntity.getEyeHeight(),
            closestEntity.getZ(),
            (float) citizen.getMaxHeadYRot(),
            (float) citizen.getMaxHeadXRot());
        return MourningState.STARING;
    }

    /**
     * Decide what to do next.
     *
     * @return the next state to go to.
     */
    private IState decide()
    {
        if (!citizen.getNavigation().isDone())
        {
            return CitizenAIState.IDLE;
        }

        if (this.citizen.getRandom().nextBoolean())
        {
            return MourningState.STARING;
        }

        if (this.graveyard == null)
        {
            final IBuilding graveyardBuilding =
              citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getFirstBuildingMatching(b -> b instanceof BuildingGraveyard && b.getFirstModuleOccurance(
                GraveyardManagementModule.class).hasRestingCitizen(citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens()));
            if (graveyardBuilding != null)
            {
                this.graveyard = graveyardBuilding.getPosition();
            }
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

    public void reset()
    {
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
        final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
        if (colony != null && colony.getBuildingManager().hasTownHall())
        {
            return colony.getBuildingManager().getTownHall().getStandingPosition();
        }

        return citizen.getCitizenData().getHomePosition();
    }
}
