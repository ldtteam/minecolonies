package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.minecolonies.coremod.entity.ai.minimal.EntityAIEatTask.EatingState.CHECK_FOR_FOOD;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends Goal
{
    /**
     * The different types of AIStates related to eating.
     */
    public enum WanderState implements IState
    {
        IDLE,
        WANDER,
        GO_TO_LEISURE_SITE,
        WANDER_AT_LEISURE_SITE,
        SIT_AT_LEISURE_SITE,
        GO_TO_LIBRARY,
        READ_A_BOOK
    }

    /**
     * The citizen that is wandering.
     */
    protected final AbstractEntityCitizen citizen;

    /**
     * Wandering speed.
     */
    protected final double  speed;

    /**
     * AI statemachine
     */
    private final TickRateStateMachine<WanderState> stateMachine;

    /**
     * Instantiates this task.
     *
     * @param citizen        the citizen.
     * @param speed          the speed.
     */
    public EntityAICitizenWander(final AbstractEntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(WanderState.IDLE, e -> Log.getLogger().warn("Wandering AI threw exception:", e));
        stateMachine.addTransition(new TickingTransition<>(WanderState.IDLE, () -> true, this::decide, 20));
        // don't forget tavern.
    }

    private WanderState decide()
    {
        final int randomBit = citizen.getRandom().nextInt(100);
        if (randomBit < 5)
        {
            return WanderState.GO_TO_LIBRARY;
        }
        else if (randomBit < 35)
        {
            return WanderState.GO_TO_LEISURE_SITE;
        }

        return WanderState.WANDER;
    }

    @Override
    public boolean canUse()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP)
        {
            return false;
        }
        stateMachine.tick();
        return citizen.getRandom().nextInt(Constants.TICKS_SECOND * 3) == 0 && citizen.getDesiredActivity() != DesiredActivity.SLEEP && citizen.getNavigation().isDone();
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    @Override
    public boolean canContinueToUse()
    {
        return !citizen.getNavigation().isDone();
    }

    @Override
    public void start()
    {
        citizen.getNavigation().moveToRandomPos(10, this.speed);
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {

    }

    @Override
    public void stop()
    {
        reset();
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
    }
}
