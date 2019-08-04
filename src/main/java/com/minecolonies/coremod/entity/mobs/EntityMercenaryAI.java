package com.minecolonies.coremod.entity.mobs;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.ldtteam.blockout.Log;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class EntityMercenaryAI extends EntityAIBase
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
    private final TickRateStateMachine stateMachine;

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

    public enum State implements IAIState
    {
        INIT,
        SPAWN_EVENT,
        PATROLLING,
        FIGHTING,
        ALIVE,
        DEAD;

        @Override
        public boolean isOkayToEat()
        {
            return false;
        }
    }

    public EntityMercenaryAI(final EntityMercenary entityMercenary)
    {
        super();
        entity = entityMercenary;
        patrolPoints = new LinkedList<>();
        stateMachine = new TickRateStateMachine(State.INIT, this::handleAIException);
        stateMachine.addTransition(new TickingTransition(State.INIT, this::initialize, () -> State.PATROLLING, 10));
        stateMachine.addTransition(new TickingTransition(State.PATROLLING, this::hasTarget, () -> State.FIGHTING, 5));
        stateMachine.addTransition(new TickingTransition(State.PATROLLING, this::patrol, () -> State.PATROLLING, 10));
        stateMachine.addTransition(new TickingTransition(State.FIGHTING, this::fighting, () -> State.PATROLLING, 5));
    }

    /**
     * Initializes the AI
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
     */
    private boolean hasTarget()
    {
        if (entity.getAttackTarget() != null && !entity.getAttackTarget().isDead)
        {
            entity.getAttackTarget().setRevengeTarget(entity);
            return true;
        }
        return false;
    }

    /**
     * Patrols the buildings and random points. Attempts to steal from buildings.
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
                        entity.swingArm(Hand.OFF_HAND);
                        LanguageHandler.sendPlayersMessage(entity.getColony().getMessageEntityPlayers(),
                          "com.minecolonies.coremod.mercenary.stealBuilding",
                          stack.getDisplayName());
                    }
                }
            }

            if (rand.nextInt(4) == 0)
            {
                movingToBuilding = true;
                currentPatrolPos = patrolPoints.get(rand.nextInt(patrolPoints.size()));
            }
            else
            {
                movingToBuilding = false;
                currentPatrolPos = BlockPosUtil.getRandomPosition(entity.getEntityWorld(), entity.getPosition(), entity.getPosition());
            }
        }

        if (entity.getPosition().equals(lastWorkerPos))
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
            entity.getNavigator().clearPath();
        }

        lastWorkerPos = entity.getPosition();

        return true;
    }

    /**
     * Fighting against a target
     */
    private boolean fighting()
    {
        if (entity.getAttackTarget() == null || entity.getAttackTarget().isDead)
        {
            entity.getNavigator().clearPath();
            attackPath = null;
            return true;
        }

        if (attacktimer > 0)
        {
            attacktimer--;
        }

        if (attackPath == null || !attackPath.isInProgress())
        {
            entity.getNavigator().moveToLivingEntity(entity.getAttackTarget(), 1);
            entity.getLookHelper().setLookPositionWithEntity(entity.getAttackTarget(), 180f, 180f);
        }

        final int distance = BlockPosUtil.getMaxDistance2D(entity.getPosition(), entity.getAttackTarget().getPosition());

        // Check if we can attack
        if (distance < MELEE_ATTACK_DIST && attacktimer == 0)
        {
            entity.swingArm(Hand.MAIN_HAND);
            entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.55f, 1.0f);
            entity.getAttackTarget().attackEntityFrom(new EntityDamageSource(entity.getName(), entity), 15);
            entity.getAttackTarget().setFire(3);
            attacktimer = ATTACK_DELAY;
        }
        else if (distance > MAX_BLOCK_CHASE_DISTANCE)
        {
            entity.setAttackTarget(null);
            entity.getNavigator().clearPath();
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
    public boolean shouldExecute()
    {
        return entity != null && !entity.isDead && !entity.isInvisible() && entity.getColony() != null && entity.getState() == State.ALIVE;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        stateMachine.tick();
        return entity != null && !entity.isDead && !entity.isInvisible() && entity.getColony() != null && entity.getState() == State.ALIVE;
    }
}
