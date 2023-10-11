package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.combat.CombatAIStates;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
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
    private final AbstractEntityMinecoloniesMob raider;

    /**
     * Target block we're walking to
     */
    private BlockPos targetBlock = null;

    /**
     * Campfire walk timer
     */
    private long walkTimer = 0;

    public RaiderWalkAI(final AbstractEntityMinecoloniesMob raider, final ITickRateStateMachine<IState> stateMachine)
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

            if (targetBlock == null || raider.blockPosition().distSqr(targetBlock) < 25 || raider.level.getGameTime() > walkTimer)
            {
                targetBlock = raider.getColony().getRaiderManager().getRandomBuilding();
                walkTimer = raider.level.getGameTime() + TICKS_SECOND * 240;

                final List<BlockPos> wayPoints = ((IColonyRaidEvent) event).getWayPoints();
                final BlockPos moveToPos = ShipBasedRaiderUtils.chooseWaypointFor(wayPoints, raider.blockPosition(), targetBlock);
                raider.getNavigation().moveToXYZ(moveToPos.getX(), moveToPos.getY(), moveToPos.getZ(), !moveToPos.equals(targetBlock) && moveToPos.distManhattan(wayPoints.get(0)) > 50 ? 1.8 : 1.1);
            }
            else if (raider.getNavigation().isDone() || raider.getNavigation().getDesiredPos() == null)
            {
                final List<BlockPos> wayPoints = ((IColonyRaidEvent) event).getWayPoints();
                final BlockPos moveToPos = ShipBasedRaiderUtils.chooseWaypointFor(wayPoints, raider.blockPosition(), targetBlock);
                raider.getNavigation()
                  .moveToXYZ(moveToPos.getX(), moveToPos.getY(), moveToPos.getZ(), !moveToPos.equals(targetBlock) && moveToPos.distManhattan(wayPoints.get(0)) > 50 ? 1.8 : 1.1);
            }
        }

        return false;
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
        targetBlock = BlockPosUtil.getRandomPosition(raider.level,
          campFire,
          BlockPos.ZERO,
          3,
          6);
        if (targetBlock != null && targetBlock != BlockPos.ZERO)
        {
            raider.getNavigation().moveToXYZ(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0);
        }
    }
}

