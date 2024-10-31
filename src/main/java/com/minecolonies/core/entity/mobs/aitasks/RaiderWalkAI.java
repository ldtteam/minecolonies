package com.minecolonies.core.entity.mobs.aitasks;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.pathfinding.IPathJob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.events.raid.HordeRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * AI for handling the raiders walking directions
 */
public class RaiderWalkAI implements IStateAI
{
    /**
     * The entity using this AI
     */
    private final AbstractEntityRaiderMob raider;

    /**
     * Target block we're walking to
     */
    private BlockPos targetBlock = null;

    /**
     * Campfire walk timer
     */
    private long walkTimer = 0;

    /**
     * Random path result.
     */
    private PathResult<? extends IPathJob> randomPathResult;

    /**
     * If we are currently trying to move to a random block.
     */
    private boolean walkInBuildingState = false;

    public RaiderWalkAI(final AbstractEntityRaiderMob raider, final ITickRateStateMachine<IState> stateMachine)
    {
        this.raider = raider;
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.NO_TARGET, this::walk, () -> null, 80));
    }

    /**
     * Walk raider towards the colony or campfires
     *
     * @return
     */
    private boolean walk()
    {
        if (raider.getColony() != null)
        {
            final IColonyEvent event = raider.getColony().getEventManager().getEventByID(raider.getEventID());
            if (event == null)
            {
                return false;
            }

            if (event.getStatus() == EventStatus.PREPARING && event instanceof HordeRaidEvent)
            {
                walkToCampFire();
                return false;
            }
            raider.setTempEnvDamageImmunity(false);

            if (targetBlock == null || raider.level.getGameTime() > walkTimer)
            {
                targetBlock = raider.getColony().getRaiderManager().getRandomBuilding();
                walkTimer = raider.level.getGameTime() + TICKS_SECOND * 240;

                final List<BlockPos> wayPoints = ((IColonyRaidEvent) event).getWayPoints();
                final BlockPos moveToPos = ShipBasedRaiderUtils.chooseWaypointFor(wayPoints, raider.blockPosition(), targetBlock);
                raider.getNavigation().moveToXYZ(moveToPos.getX(), moveToPos.getY(), moveToPos.getZ(), !moveToPos.equals(targetBlock) && moveToPos.distManhattan(wayPoints.get(0)) > 50 ? 1.8 : 1.1);
                walkInBuildingState = false;
                randomPathResult = null;
            }
            else if (walkInBuildingState)
            {
                final BlockPos moveToPos = findRandomPositionToWalkTo();
                if (moveToPos != null)
                {
                    if (moveToPos == BlockPos.ZERO)
                    {
                        walkInBuildingState = false;
                        targetBlock = null;
                        return false;
                    }
                    raider.getNavigation().moveToXYZ(moveToPos.getX(), moveToPos.getY(), moveToPos.getZ(), 0.9);
                    if (raider.blockPosition().distSqr(moveToPos) < 4)
                    {
                        if (raider.getRandom().nextDouble() < 0.25)
                        {
                            walkInBuildingState = false;
                            targetBlock = null;
                        }
                        else
                        {
                            randomPathResult = null;
                            walkTimer = raider.level.getGameTime() + TICKS_SECOND * 60;
                            findRandomPositionToWalkTo();
                        }
                    }
                }
            }
            else if (raider.blockPosition().distSqr(targetBlock) < 25)
            {
                findRandomPositionToWalkTo();
                walkTimer = raider.level.getGameTime() + TICKS_SECOND * 30;
                walkInBuildingState = true;
            }
            else if (raider.getNavigation().isDone() || raider.getNavigation().getDesiredPos() == null)
            {
                final List<BlockPos> wayPoints = ((IColonyRaidEvent) event).getWayPoints();
                final BlockPos moveToPos = ShipBasedRaiderUtils.chooseWaypointFor(wayPoints, raider.blockPosition(), targetBlock);

                if (moveToPos.equals(BlockPos.ZERO))
                {
                    Log.getLogger().warn("Raider trying to path to zero position, target pos:" + targetBlock + " Waypoints:");
                    for (final BlockPos pos : wayPoints)
                    {
                        Log.getLogger().warn(pos.toShortString());
                    }
                }

                raider.getNavigation()
                  .moveToXYZ(moveToPos.getX(), moveToPos.getY(), moveToPos.getZ(), !moveToPos.equals(targetBlock) && moveToPos.distManhattan(wayPoints.get(0)) > 50 ? 1.8 : 1.1);
            }
        }

        return false;
    }

    protected BlockPos findRandomPositionToWalkTo()
    {
        if (randomPathResult == null || randomPathResult.failedToReachDestination())
        {
            if (raider.getColony().getBuildingManager().getBuilding(targetBlock) instanceof AbstractBuilding building
                  && building.getBuildingLevel() > 0
                  && !building.getCorners().getA().equals(building.getCorners().getB()))
            {
                randomPathResult = raider.getNavigation().moveToRandomPos(10, 0.9, building.getCorners());
                if (randomPathResult != null)
                {
                    randomPathResult.getJob().getPathingOptions().withCanEnterDoors(true).withToggleCost(0).withNonLadderClimbableCost(0);
                }
            }
            else
            {
                return BlockPos.ZERO;
            }
        }

        if (randomPathResult.isPathReachingDestination())
        {
            return randomPathResult.getPath().getEndNode().asBlockPos();
        }

        if (randomPathResult.isCancelled())
        {
            randomPathResult = null;
            return null;
        }

        return null;
    }

    /**
     * Chooses and walks to a random campfire
     */
    private void walkToCampFire()
    {
        if (raider.level.getGameTime() - walkTimer < 0)
        {
            return;
        }

        final BlockPos campFire = ((HordeRaidEvent) raider.getColony().getEventManager().getEventByID(raider.getEventID())).getRandomCampfire();

        if (campFire == null)
        {
            return;
        }

        walkTimer = raider.level.getGameTime() + raider.level.random.nextInt(1000);
        final BlockPos posAroundCampfire = BlockPosUtil.getRandomPosition(raider.level,
          campFire,
          BlockPos.ZERO,
          3,
          6);
        if (posAroundCampfire != null && posAroundCampfire != BlockPos.ZERO)
        {
            raider.getNavigation().moveToXYZ(posAroundCampfire.getX(), posAroundCampfire.getY(), posAroundCampfire.getZ(), 1.0);
        }
    }
}

