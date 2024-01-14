package com.minecolonies.core.entity.mobs;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.sounds.MercenarySounds;
import com.minecolonies.api.util.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_MERCENARY_STEAL_BUILDING;

public class EntityMercenaryAI extends Goal
{
    /**
     * The max distance a mercenary chases his target.
     */
    private static final int MAX_BLOCK_CHASE_DISTANCE = 50;

    /**
     * The max amount of AI - invocations during pathing till the mercenary is considered stuck.
     */
    private static final int MAX_STUCK_TIME = 10;

    /**
     * State machine for this AI
     */
    private final ITickRateStateMachine<IState> stateMachine;

    /**
     * The entity for this AI.
     */
    private final EntityMercenary entity;

    /**
     * The points the mercenaries are checking out.
     */
    private final List<BlockPos> patrolPoints;

    /**
     * The current patrolling position.
     */
    private BlockPos currentPatrolPos;

    /**
     * Whether we're currently moving to a building
     */
    private boolean movingToBuilding = false;

    /**
     * The position the entity was on last tick
     */
    private BlockPos lastWorkerPos;

    /**
     * The path for attacking.
     */
    private PathResult attackPath;

    /**
     * The timer for attacks
     */
    private int attacktimer = 0;

    /**
     * The delay between attacks, in 5ticks
     */
    private static final int ATTACK_DELAY = 5;

    /**
     * The distance needed to be able to attack
     */
    private static final int MELEE_ATTACK_DIST = 2;

    /**
     * Timer to check for stuck
     */
    private int stuckTimer = 0;

    private final Random rand = new Random();

    public enum State implements IState
    {
        INIT,
        SPAWN_EVENT,
        PATROLLING,
        FIGHTING,
        ALIVE,
        DEAD;
    }

    public EntityMercenaryAI(final EntityMercenary entityMercenary)
    {
        super();
        entity = entityMercenary;
        patrolPoints = new LinkedList<>();
        stateMachine = new TickRateStateMachine<>(State.INIT, this::handleAIException);
        stateMachine.addTransition(new TickingTransition<>(State.INIT, this::initialize, () -> State.PATROLLING, 10));
        stateMachine.addTransition(new TickingTransition<>(State.PATROLLING, this::hasTarget, () -> State.FIGHTING, 5));
        stateMachine.addTransition(new TickingTransition<>(State.PATROLLING, this::patrol, () -> State.PATROLLING, 10));
        stateMachine.addTransition(new TickingTransition<>(State.FIGHTING, this::fighting, () -> State.PATROLLING, 5));
    }

    /**
     * Initializes the AI
     *
     * @return whether the ai was initialized successfully
     */
    private boolean initialize()
    {
        if (entity.getColony() == null)
        {
            return false;
        }

        patrolPoints.addAll(entity.getColony().getBuildingManager().getBuildings().keySet());
        return true;
    }

    /**
     * Check if we got a target to fight
     *
     * @return whether we got a target to fight
     */
    private boolean hasTarget()
    {
        if (entity.getTarget() != null && entity.getTarget().isAlive()
              && !(entity.getTarget() instanceof EntityMercenary))
        {
            entity.getTarget().setLastHurtByMob(entity);
            return true;
        }
        return false;
    }

    /**
     * Patrols the buildings and random points. Attempts to steal from buildings.
     *
     * @return true
     */
    private boolean patrol()
    {
        if (currentPatrolPos == null || entity.getProxy().walkToBlock(currentPatrolPos, 3, true))
        {
            if (currentPatrolPos != null && movingToBuilding)
            {
                // Attempt to steal!
                final IBuilding building = entity.getColony().getBuildingManager().getBuilding(currentPatrolPos);

                if (building != null)
                {
                    final List<IItemHandler> handlers = new ArrayList<>(InventoryUtils.getItemHandlersFromProvider(building.getTileEntity()));
                    final IItemHandler handler = handlers.get(rand.nextInt(handlers.size()));
                    final ItemStack stack = handler.extractItem(rand.nextInt(handler.getSlots()), 5, false);

                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        entity.swing(InteractionHand.OFF_HAND);
                        MessageUtils.format(MESSAGE_INFO_COLONY_MERCENARY_STEAL_BUILDING, stack.getHoverName().getString()).sendTo(entity.getColony()).forAllPlayers();
                    }
                }
            }

            if (rand.nextInt(4) == 0 && !patrolPoints.isEmpty())
            {
                movingToBuilding = true;
                currentPatrolPos = patrolPoints.get(rand.nextInt(patrolPoints.size()));
            }
            else
            {
                movingToBuilding = false;
                currentPatrolPos = BlockPosUtil.getRandomPosition(entity.getCommandSenderWorld(), entity.blockPosition(), entity.blockPosition(), 10, 27);
            }
        }

        if (entity.blockPosition().equals(lastWorkerPos))
        {
            stuckTimer++;
        }
        else
        {
            stuckTimer = 0;
        }

        if (stuckTimer > MAX_STUCK_TIME)
        {
            stuckTimer = 0;
            currentPatrolPos = null;
            entity.getNavigation().stop();
        }

        lastWorkerPos = entity.blockPosition();

        return true;
    }

    /**
     * Fighting against a target
     *
     * @return false if we are still fighting
     */
    private boolean fighting()
    {
        if (entity.getTarget() == null || !entity.getTarget().isAlive())
        {
            entity.getNavigation().stop();
            attackPath = null;
            return true;
        }

        if (attacktimer > 0)
        {
            attacktimer--;
        }

        if (attackPath == null || !attackPath.isInProgress())
        {
            entity.getNavigation().moveToLivingEntity(entity.getTarget(), 1);
            entity.getLookControl().setLookAt(entity.getTarget(), 180f, 180f);
        }

        final int distance = BlockPosUtil.getMaxDistance2D(entity.blockPosition(), entity.getTarget().blockPosition());

        // Check if we can attack
        if (distance < MELEE_ATTACK_DIST && attacktimer == 0)
        {
            entity.swing(InteractionHand.MAIN_HAND);
            entity.playSound(MercenarySounds.mercenaryAttack, 0.55f, 1.0f);
            entity.getTarget().hurt(entity.level.damageSources().mobAttack(entity), 15);
            entity.getTarget().setSecondsOnFire(3);
            attacktimer = ATTACK_DELAY;
        }
        else if (distance > MAX_BLOCK_CHASE_DISTANCE)
        {
            entity.setTarget(null);
            entity.getNavigation().stop();
            attackPath = null;
            return true;
        }

        return false;
    }

    private void handleAIException(final RuntimeException e)
    {
        Log.getLogger().error("MercenaryAI threw an exception:", e);
    }

    @Override
    public boolean canUse()
    {
        return entity != null && entity.isAlive() && !entity.isInvisible() && entity.getColony() != null && entity.getState() == State.ALIVE;
    }

    @Override
    public boolean canContinueToUse()
    {
        stateMachine.tick();
        return entity != null && entity.isAlive() && !entity.isInvisible() && entity.getColony() != null && entity.getState() == State.ALIVE;
    }
}
