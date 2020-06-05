package com.minecolonies.api.entity.ai.statemachine.basestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.states.IStateEventType;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineEvent;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineOneTimeEvent;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineTransition;
import com.minecolonies.api.util.Log;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Basic statemachine class, can be used for any Transition typed which extends the transition interface.
 * It contains the current state and a hashmap for events and transitions, which are the minimal requirements to have a working statemachine.
 */
public class BasicStateMachine<T extends IStateMachineTransition<S>, S extends IState> implements IStateMachine<T, S>
{
    /**
     * The lists of transitions and events
     */
    @NotNull
    protected final Map<S, ArrayList<T>>               transitionMap;
    @NotNull
    protected final Map<IStateEventType, ArrayList<T>> eventTransitionMap;

    /**
     * The current state we're in
     */
    @NotNull
    private S state;

    /**
     * The state we started in
     */
    @NotNull
    private final S initState;

    /**
     * The exception handler
     */
    @NotNull
    private final Consumer<RuntimeException> exceptionHandler;

    /**
     * Construct a new StateMachine
     * @param initialState the initial state.
     * @param exceptionHandler the exception handler.
     */
    protected BasicStateMachine(@NotNull final S initialState, @NotNull final Consumer<RuntimeException> exceptionHandler)
    {
        this.state = initialState;
        this.initState = initialState;
        this.exceptionHandler = exceptionHandler;
        this.transitionMap = new HashMap<>();
        this.transitionMap.put(initialState, new ArrayList<>());
        this.eventTransitionMap = new HashMap<>();
    }

    /**
     * Add one transition
     *
     * @param transition the transition to add
     */
    public void addTransition(final T transition)
    {
        if (transition.getState() != null)
        {
            transitionMap.computeIfAbsent(transition.getState(), k -> new ArrayList<>()).add(transition);
        }
        if (transition instanceof IStateMachineEvent)
        {
            eventTransitionMap.computeIfAbsent(((IStateMachineEvent<?>) transition).getEventType(), k -> new ArrayList<>()).add(transition);
        }
    }

    /**
     * Unregisters a transition
     */
    public void removeTransition(final T transition)
    {
        if (transition instanceof IStateMachineEvent)
        {
            final ArrayList<T> temp = new ArrayList<>(eventTransitionMap.get(((IStateMachineEvent<?>) transition).getEventType()));
            temp.remove(transition);
            eventTransitionMap.put(((IStateMachineEvent<?>) transition).getEventType(), temp);
        }
        else
        {
            final ArrayList<T> temp = new ArrayList<>(transitionMap.get(transition.getState()));
            temp.remove(transition);
            transitionMap.put(transition.getState(), temp);
        }
    }

    /**
     * Updates the statemachine.
     */
    public void tick()
    {
        // Check if any Events happens before doing state transitions
        if (!eventTransitionMap.values().stream().anyMatch(k -> k.stream().anyMatch(this::checkTransition)))
        {
            // State transitions
            transitionMap.get(state).stream().anyMatch(this::checkTransition);
        }
    }

    /**
     * Check the condition for a transition
     *
     * @param transition the target to check
     * @return true if this target worked and we should stop executing this tick
     */
    public boolean checkTransition(@NotNull final T transition)
    {
        try
        {
            if (!transition.checkCondition())
            {
                return false;
            }
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Condition check for transition " + transition + " threw an exception:", e);
            this.onException(e);
            return false;
        }
        return transitionToNext(transition);
    }

    /**
     * Continuation of checkTransition.
     * applies the transition and changes the state.
     * if the state is null, execute more transitions
     * and don't change state.
     *
     * @param transition the transitions we're looking at
     * @return true if did transition to a new state
     */
    public boolean transitionToNext(@NotNull final T transition)
    {
        final S newState;
        try
        {
            newState = transition.getNextState();
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Statemachine for transition " + transition + " threw an exception:", e);
            this.onException(e);
            return false;
        }

        if (newState != null)
        {
            if (transition instanceof IStateMachineOneTimeEvent && ((IStateMachineOneTimeEvent<?>) transition).shouldRemove())
            {
                removeTransition(transition);
            }

            state = newState;
            return true;
        }
        return false;
    }

    /**
     * Handle an exception higher up.
     *
     * @param e The exception to be handled.
     */
    protected void onException(final RuntimeException e)
    {
        exceptionHandler.accept(e);
    }

    /**
     * Get the current state of the statemachine
     *
     * @return The current IAIState.
     */
    public final S getState()
    {
        return state;
    }

    /**
     * Resets the statemachine
     */
    public void reset()
    {
        state = initState;
    }
}
