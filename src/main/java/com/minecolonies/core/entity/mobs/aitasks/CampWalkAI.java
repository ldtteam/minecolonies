package com.minecolonies.core.entity.mobs.aitasks;

import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.api.entity.pathfinding.IPathJob;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * AI for handling the raiders walking directions
 */
public class CampWalkAI implements IStateAI
{
    /**
     * The entity using this AI
     */
    private final AbstractEntityMinecoloniesMonster entity;

    /**
     * Target block we're walking to
     */
    private BlockPos targetBlock = null;

    /**
     * Walk timer
     */
    private long walkTimer = 0;

    /**
     * Random path result.
     */
    private PathResult<? extends IPathJob> randomPathResult;

    /**
     * Spawn center box cache.
     */
    private Tuple<BlockPos, BlockPos> spawnCenterBoxCache = null;

    public CampWalkAI(final AbstractEntityMinecoloniesMonster raider, final ITickRateStateMachine<IState> stateMachine)
    {
        this.entity = raider;
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.NO_TARGET, this::walk, () -> null, 80));
    }

    /**
     * Walk raider towards the colony or campfires
     *
     */
    private boolean walk()
    {
        if (targetBlock == null || entity.level.getGameTime() > walkTimer)
        {
            targetBlock = findRandomPositionToWalkTo();
            walkTimer = entity.level.getGameTime() + TICKS_SECOND * 30;
        }
        else
        {
            entity.getNavigation().moveToXYZ(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(),1.1);
            randomPathResult = null;
        }

        return false;
    }

    protected BlockPos findRandomPositionToWalkTo()
    {
        if (randomPathResult == null || randomPathResult.failedToReachDestination())
        {
            if (spawnCenterBoxCache == null)
            {
                final BlockPos startPos = entity.getSpawnPos() == null ? entity.blockPosition() : entity.getSpawnPos();
                spawnCenterBoxCache = new Tuple<>(startPos.offset(-10,-5,-10), startPos.offset(10,5,10));
            }

            randomPathResult = entity.getNavigation().moveToRandomPos(10, 0.9, spawnCenterBoxCache);
            if (randomPathResult != null)
            {
                randomPathResult.getJob().getPathingOptions().withCanEnterDoors(true).withToggleCost(0).withNonLadderClimbableCost(0);
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
}
