package com.minecolonies.core.entity.mobs.aitasks;

import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.api.entity.pathfinding.IPathJob;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
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
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.NO_TARGET, this::walk, () -> null, TICKS_SECOND * 30));
    }

    /**
     * Walk camp mob randomly
     */
    private boolean walk()
    {
        if (spawnCenterBoxCache == null)
        {
            final BlockPos startPos = entity.getSpawnPos() == null ? entity.blockPosition() : entity.getSpawnPos();
            spawnCenterBoxCache = new Tuple<>(startPos.offset(-10, -5, -10), startPos.offset(10, 5, 10));
        }

        EntityNavigationUtils.walkToRandomPosWithin(entity, 10, 0.6, spawnCenterBoxCache);
        return false;
    }
}
