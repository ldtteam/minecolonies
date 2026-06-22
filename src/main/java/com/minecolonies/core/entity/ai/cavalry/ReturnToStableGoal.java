package com.minecolonies.core.entity.ai.cavalry;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Goal for a cavalry horse to return to its stable.
 */
public class ReturnToStableGoal extends Goal
{
    /**
     * How often the goal should refresh pathing while moving.
     */
    private static final int PATH_REFRESH_TICK_RATE = 10;

    /**
     * How often the goal should perform stable and stall lookup work.
     */
    private static final int LOOKUP_TICK_RATE = 20;

    /**
     * The cavalry horse entity.
     */
    private final CavalryHorseEntity horse;

    /**
     * The speed at which the cavalry horse moves.
     */
    private final double speed;

    /**
     * The number of ticks to wait after dismounting before returning to the stable.
     */
    private static final int LINGER_AFTER_DISMOUNT = 6000;

    /**
     * Begin returning if further than this.
     */
    private final double startDistanceSqr;

    /**
     * The stable block position.
     */
    private BlockPos targetStable = null;

    /**
     * The stall block position.
     */
    private BlockPos targetStall = null;

    /**
     * Whether the horse reached the destination during the last state machine pathing step.
     */
    private boolean reachedTarget = false;

    /**
     * State machine that runs the return-to-stable behavior at controlled tick rates.
     */
    private final ITickRateStateMachine<State> stateMachine;

    /**
     * States for the return-to-stable behavior.
     */
    private enum State implements IState
    {
        INIT,
        WALK_TO_STABLE,
        FIND_STALL,
        WALK_TO_STALL,
        DONE
    }

    /**
     * Creates a new ReturnToStableGoal.
     *
     * @param horse The cavalry horse entity.
     * @param speed The speed at which the cavalry horse moves.
     * @param startDistance The distance at which the cavalry horse starts returning to the stable.
     */
    public ReturnToStableGoal(final CavalryHorseEntity horse, final double speed, final double startDistance)
    {
        this.horse = horse;
        this.speed = speed;
        this.startDistanceSqr = startDistance * startDistance;
        this.stateMachine = new TickRateStateMachine<>(State.INIT, this::handleAIException);
        this.stateMachine.addTransition(
          new TickingTransition<>(State.INIT, this::resolveStable, () -> State.WALK_TO_STABLE, LOOKUP_TICK_RATE));
        this.stateMachine.addTransition(
          new TickingTransition<>(State.WALK_TO_STABLE, this::walkToStable, this::nextAfterStableWalk, PATH_REFRESH_TICK_RATE));
        this.stateMachine.addTransition(
          new TickingTransition<>(State.FIND_STALL, this::findStall, () -> State.WALK_TO_STALL, LOOKUP_TICK_RATE));
        this.stateMachine.addTransition(
          new TickingTransition<>(State.WALK_TO_STALL, this::walkToStall, this::nextAfterStallWalk, PATH_REFRESH_TICK_RATE));
        this.stateMachine.addTransition(
          new TickingTransition<>(State.DONE, () -> true, () -> State.DONE, LOOKUP_TICK_RATE));
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Check if the goal can be used.
     * <p>
     * Conditions to return false:
     * <ul>
     *     <li>The horse has a passenger.</li>
     *     <li>The horse does not have a stable block position.</li>
     *     <li>The horse is within the start distance (blocks) of the stable block position.</li>
     * </ul>
     * <p>
     * If the goal is usable, the stable block position is set to be the immutable target block position.
     *
     * @return true if the goal can be used, false otherwise
     */
    @Override
    public boolean canUse()
    {
        if (!canStillRun() || horse.isInStable())
        {
            return false;
        }

        long lastDismountTime = horse.getLastDismountTime();

        if (lastDismountTime > 0 && horse.level().getGameTime() - lastDismountTime < LINGER_AFTER_DISMOUNT)
        {
            return false;
        }

        validateHomeStable();

        IBuilding building = horse.getStableBuilding();
        if (!(building instanceof BuildingStable))
        {
            return false;
        }

        double distSqr = horse.distanceToSqr(building.getPosition().getX() + 0.5, building.getPosition().getY() + 0.5, building.getPosition().getZ() + 0.5);

        if (distSqr <= startDistanceSqr)
        {
            return false;
        }

        targetStable = building.getPosition().immutable();

        return true;
    }

    /**
     * Validates the home stable of the horse.
     * <p>
     * If the horse is on the client side, does nothing.
     * If no colony is found at the horse's position, sets the horse's home building to null.
     * If no stable is found at the horse's position, sets the horse's home building to null.
     * Otherwise, sets the horse's home building to the found stable.
     */
    private void validateHomeStable()
    {
        if (horse.level().isClientSide)
        {
            return;
        }

        IBuilding building = horse.getAnimalData().getHomeBuilding();

        // If the horse's stable is already built, do nothing
        if (building instanceof BuildingStable && building.isBuilt())
        {
            return;
        }

        IColony colony = IColonyManager.getInstance().getClosestColony(horse.level(), horse.blockPosition());

        // No colony found - set the stable to null
        if (colony == null) 
        {
            horse.getAnimalData().setHomeBuilding(null);
            return;
        }

        BlockPos stablePos = colony.getServerBuildingManager().getBestBuilding(horse.blockPosition(), BuildingStable.class);

        // No stable found - set the stable to null
        if (stablePos == null) 
        {
            horse.getAnimalData().setHomeBuilding(null);
            return;
        }

        // Set the stable as the horse's home
        building = colony.getServerBuildingManager().getBuilding(stablePos);

        horse.getAnimalData().setHomeBuilding(building);
    }

    /**
     * Checks if the goal can continue to be used.
     *
     * @return true if the state machine is not done and the horse can still be controlled by this goal.
     */
    @Override
    public boolean canContinueToUse()
    {
        return stateMachine.getState() != State.DONE && canStillRun();
    }

    /**
     * Resets the return-to-stable state before the goal starts running.
     */
    @Override
    public void start()
    {
        targetStall = null;
        reachedTarget = false;
        stateMachine.reset();
    }

    /**
     * Stops the horse navigation goal and resets return-to-stable targets.
     */
    @Override
    public void stop()
    {
        targetStable = null;
        targetStall = null;
        reachedTarget = false;
    }

    /**
     * Ticks the return-to-stable state machine.
     */
    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    /**
     * Checks whether the horse is still eligible to run this goal.
     *
     * @return true if the goal may continue running.
     */
    private boolean canStillRun()
    {
        return !horse.level().isClientSide
                 && horse.getControllingPassenger() == null
                 && !horse.hasReservation()
                 && horse.getAnimalData() != null;
    }

    /**
     * Resolves and caches the horse's stable position.
     *
     * @return true when a valid stable target is available.
     */
    private boolean resolveStable()
    {
        if (!canStillRun())
        {
            return true;
        }

        validateHomeStable();

        IBuilding building = horse.getStableBuilding();
        if (!(building instanceof BuildingStable))
        {
            return true;
        }

        targetStable = building.getPosition().immutable();
        return true;
    }

    /**
     * Refreshes pathing to the stable position.
     *
     * @return true after the state machine step has been processed.
     */
    private boolean walkToStable()
    {
        if (!canStillRun() || targetStable == null)
        {
            reachedTarget = true;
            return true;
        }

        reachedTarget = EntityNavigationUtils.walkToPos(horse, targetStable, 2, false, speed);
        return true;
    }

    /**
     * Chooses the next state after a stable pathing step.
     *
     * @return the next return-to-stable state.
     */
    private State nextAfterStableWalk()
    {
        if (!canStillRun())
        {
            return horse.isInStable() ? State.FIND_STALL : State.DONE;
        }

        return horse.isInStable() || reachedTarget ? State.FIND_STALL : State.WALK_TO_STABLE;
    }

    /**
     * Finds and caches the stall position inside the horse's stable.
     *
     * @return true after the stall lookup has been processed.
     */
    private boolean findStall()
    {
        IBuilding building = horse.getStableBuilding();
        if (!(building instanceof BuildingStable stable))
        {
            return true;
        }

        targetStable = building.getPosition().immutable();
        targetStall = stable.getNextStallPosition();
        return true;
    }

    /**
     * Refreshes pathing to the cached stall position.
     *
     * @return true after the state machine step has been processed.
     */
    private boolean walkToStall()
    {
        if (targetStall == null)
        {
            reachedTarget = true;
            return true;
        }

        reachedTarget = EntityNavigationUtils.walkToPos(horse, targetStall, 2, false, speed);
        return true;
    }

    /**
     * Chooses the next state after a stall pathing step.
     *
     * @return the next return-to-stable state.
     */
    private State nextAfterStallWalk()
    {
        if (targetStall == null)
        {
            return State.DONE;
        }

        return reachedTarget ? State.DONE : State.WALK_TO_STALL;
    }

    /**
     * Handles exceptions thrown by the return-to-stable state machine.
     *
     * @param e the exception thrown while ticking the state machine.
     */
    private void handleAIException(final RuntimeException e)
    {
        Log.getLogger().error("ReturnToStableGoal threw an exception:", e);
    }
}
