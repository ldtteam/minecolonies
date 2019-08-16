package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import com.minecolonies.api.entity.ai.statemachine.basestatemachine.BasicStateMachine;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;

/**
 * Statemachine with an added tickrate limiting of transitions, allowing transitions to be checked at a lower rate.
 * Default tickrate is 20 tps (Minecraft default).
 */
public class TickRateStateMachine extends BasicStateMachine<ITickingTransition> implements ITickRateStateMachine
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
        if (tickCounter > MAX_TICKRATE)
        {
            tickCounter = 1;
        }

        if (!eventTransitionMap.get(AIBlockingEventType.AI_BLOCKING).stream().anyMatch(this::checkTransition)
              && !eventTransitionMap.get(AIBlockingEventType.EVENT).stream().anyMatch(this::checkTransition)
              && !eventTransitionMap.get(AIBlockingEventType.STATE_BLOCKING).stream().anyMatch(this::checkTransition))
        {
            if (!transitionMap.containsKey(getState()))
            {
                // Reached Trap/Sink state we cannot leave.
                onException(new RuntimeException("Missing AI transition for state: " + getState()));
                reset();
                return;
            }
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
    public boolean checkTransition(@NotNull final ITickingTransition transition)
    {
        // Check if the target should be run this Tick
        if ((tickCounter % transition.getTickRate()) != transition.getTickOffset())
        {
            return false;
        }
        return super.checkTransition(transition);
    }
}
