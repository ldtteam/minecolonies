package com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.coremod.entity.ai.statemachine.basestatemachine.BasicStateMachine;
import com.minecolonies.coremod.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.coremod.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE_VARIANT;

public class TickRateStateMachine extends BasicStateMachine<TickingTransition>
{
    /**
     * Counter keeping track of ticks
     */
    private int tickCounter = 0;

    /**
     * Construct a new StateMachine
     */
    public TickRateStateMachine(@NotNull final IAIState initialState, @NotNull final Consumer<RuntimeException> exceptionHandler)
    {
        super(initialState, exceptionHandler);

        // Initial Lists
        this.eventTransitionMap.put(AIBlockingEventType.AI_BLOCKING, new ArrayList<>());
        this.eventTransitionMap.put(AIBlockingEventType.STATE_BLOCKING, new ArrayList<>());
        this.eventTransitionMap.put(AIBlockingEventType.EVENT, new ArrayList<>());
    }

    /**
     * Tick the statemachine.
     */
    @Override
    public void tick()
    {
        tickCounter++;
        if (tickCounter >= MAX_TICKRATE + MAX_TICKRATE_VARIANT)
        {
            tickCounter = 1;
        }

        if (!eventTransitionMap.get(AIBlockingEventType.AI_BLOCKING).stream().anyMatch(this::checkTransition)
              && !eventTransitionMap.get(AIBlockingEventType.EVENT).stream().anyMatch(this::checkTransition)
              && !eventTransitionMap.get(AIBlockingEventType.STATE_BLOCKING).stream().anyMatch(this::checkTransition))
        {
            transitionMap.get(getState()).stream().anyMatch(this::checkTransition);
        }
    }

    /**
     * Check the condition for a transition
     *
     * @param transition the target to check
     * @return true if this target worked and we should stop executing this tick
     */
    @Override
    public boolean checkTransition(@NotNull final TickingTransition transition)
    {
        // Check if the target should be run this Tick
        if (((tickCounter + transition.getTickOffset()) % transition.getTickRate()) != 0)
        {
            return false;
        }
        return super.checkTransition(transition);
    }
}
