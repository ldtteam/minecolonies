package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI which controls child behaviour and growing.
 */
public class EntityAICitizenChild extends EntityAIBase
{

    /**
     * States used for this AI
     */
    public enum State implements IAIState
    {
        IDLE,
        BORED,
        PLAYING,
        VISITING,
        FOLLOWING;

        @Override
        public boolean isOkayToEat()
        {
            return true;
        }
    }

    protected final EntityCitizen child;
    private final   Random        rand = new Random();

    private final TickRateStateMachine stateMachine;

    /**
     * Timer for actions/between actions
     */
    private int actionTimer = 0;

    /**
     * Distance at which the child starts following around.
     */
    private final static int START_FOLLOW_DISTANCE = 10;

    /**
     * Timer for how long the AI is active
     */
    private int AIActiveTime = 0;

    /**
     * Minimum ticks the AI is active before it is allowed to grow
     */
    private static final int MIN_ACTIVE_TIME = 4000;

    /**
     * The entity we're following around
     */
    private WeakReference<Entity> followTarget = new WeakReference<>(null);

    /**
     * The position we started following on
     */
    private BlockPos followStart;

    /**
     * The blockpos the child is visiting
     */
    private BlockPos visitHutPos;

    /**
     * The path for visiting the hut
     */
    private PathResult visitingPath;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAICitizenChild(@NotNull final EntityCitizen citizen)
    {
        super();
        this.child = citizen;
        this.setMutexBits(1);
        stateMachine = new TickRateStateMachine(State.IDLE, this::handleAIException);
        stateMachine.addTransition(new AIEventTarget(AIBlockingEventType.STATE_BLOCKING, this::updateTimers, stateMachine::getState, 1));
        stateMachine.addTransition(new AIEventTarget(AIBlockingEventType.EVENT, this::tryGrowUp, () -> State.IDLE, 500));

        stateMachine.addTransition(new AITarget(State.IDLE, this::searchEntityToFollow, () -> State.FOLLOWING, 150));
        stateMachine.addTransition(new AITarget(State.IDLE, this::isReadyForActivity, () -> State.VISITING, 300));

        stateMachine.addTransition(new AITarget(State.FOLLOWING, this::followingEntity, 20));
        stateMachine.addTransition(new AITarget(State.VISITING, this::visitHuts, 120));
    }

    /**
     * Exception handler for the statemachine
     */
    private void handleAIException(final RuntimeException ex)
    {
        stateMachine.reset();
        Log.getLogger().warn("EntityAICitizenChild Child: " + child.getName() + " threw an exception:", ex);
    }

    /**
     * Updates all timers
     */
    private boolean updateTimers()
    {
        // Timer used for delays on actions
        if (actionTimer > 0)
        {
            actionTimer -= Configurations.gameplay.updateRate;
        }

        AIActiveTime += Configurations.gameplay.updateRate;

        return false;
    }

    /**
     * Whether the child moves to a new activity
     */
    private boolean isReadyForActivity()
    {
        return actionTimer <= 0;
    }

    /**
     * Sets the delay till the next activity can start
     */
    private void setDelayForNextAction()
    {
        // Delay next activity by 3-5min
        actionTimer = rand.nextInt(2 * 60 * 20) + 3 * 60 * 20;
    }

    /**
     * Follow activity preconditions
     * Search for someone to follow around
     */
    private boolean searchEntityToFollow()
    {
        if (!isReadyForActivity())
        {
            return false;
        }

        CompatibilityUtils.getWorldFromCitizen(child)
          // Search entities in radius
          .getEntitiesInAABBexcluding(
            child,
            child.getEntityBoundingBox().expand(
              (double) START_FOLLOW_DISTANCE,
              1.0D,
              (double) START_FOLLOW_DISTANCE),
            // Limit entity classes
            target -> target.isEntityAlive() && (target instanceof EntityAgeable || target instanceof EntityPlayer))
          // Take the first entity
          .stream()
          .findFirst()
          .ifPresent(entity -> followTarget = new WeakReference<>(entity));

        if (followTarget.get() != null)
        {
            // Follow time 30-60seconds, in ticks
            actionTimer = rand.nextInt(30 * 20) + 30 * 20;
            followStart = child.getPosition();

            return true;
        }
        return false;
    }

    /**
     * Follow an entity around
     */
    private IAIState followingEntity()
    {
        if (actionTimer <= 0 || followTarget.get() == null)
        {
            // run back to start position
            child.getNavigator().moveToXYZ(followStart.getX(), followStart.getY(), followStart.getZ(), 1.0d);

            setDelayForNextAction();
            return State.IDLE;
        }

        child.getNavigator().moveToLivingEntity(followTarget.get(), 1.0d);
        return State.FOLLOWING;
    }

    /**
     * Child visits random buildings
     */
    private IAIState visitHuts()
    {
        // Find a hut to visit
        if (visitingPath == null && child.getCitizenColonyHandler().getColony() != null)
        {
            // Visiting huts for 3min.
            if (actionTimer <= 0 && visitHutPos == null)
            {
                actionTimer = 3 * 60 * 20;
            }

            int index = child.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().size();

            index = rand.nextInt(index);

            final List<BlockPos> buildings = new ArrayList<>(child.getCitizenColonyHandler().getColony().getBuildingManager().getBuildings().keySet());
            visitHutPos = buildings.get(index);

            visitingPath = child.getNavigator().moveToXYZ(visitHutPos.getX(), visitHutPos.getY(), visitHutPos.getZ(), 1.0d);
        }

        // Visiting
        if (actionTimer > 0)
        {
            // Path got interrupted by sth
            if (visitingPath != null && !visitingPath.isInProgress())
            {
                visitingPath = child.getNavigator().moveToXYZ(visitHutPos.getX(), visitHutPos.getY(), visitHutPos.getZ(), 1.0d);
            }

            return State.VISITING;
        }

        child.getNavigator().clearPath();
        visitingPath = null;
        visitHutPos = null;
        setDelayForNextAction();

        return State.IDLE;
    }

    /**
     * Tries to grow up the child.
     *
     * @return true if it grew
     */
    private boolean tryGrowUp()
    {

        if (AIActiveTime >= MIN_ACTIVE_TIME)
        {
            if (!child.isChild())
            {
                return true;
            }

            // 1/144 Chance to grow up, every 25 seconds = avg 1h. Set to half since this AI isnt always active, e.g. sleeping.  At 2h they directly grow
            if (rand.nextInt((int) (70 / Configurations.gameplay.growthModifier)) == 0 || AIActiveTime > 70000 / Configurations.gameplay.growthModifier)
            {

                LanguageHandler.sendPlayersMessage(child.getCitizenColonyHandler().getColony().getMessageEntityPlayers(),
                  "com.minecolonies.coremod.progress.childGrow",
                  child.getName());
                // Grow up
                child.setIsChild(false);
                child.getCitizenData().setIsChild(false);
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * Returns whether the EntityAIBase should begin execution.
     * True when age less than 100, when a random (120) is chosen correctly, and when a citizen is nearby.
     */
    @Override
    public boolean shouldExecute()
    {
        return child.isChild();
    }

    /**
     * {@inheritDoc}
     * Returns whether an in-progress EntityAIBase should continue executing.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        if (child.getDesiredActivity() == DesiredActivity.SLEEP || !child.isChild())
        {
            return false;
        }

        stateMachine.tick();
        return true;
    }
}
