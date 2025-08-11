package com.minecolonies.api.entity.ai.statemachine.basestatemachine;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.states.IStateEventType;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineEvent;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineOneTimeEvent;
import com.minecolonies.api.entity.ai.statemachine.transitions.IStateMachineTransition;
import com.minecolonies.api.util.Log;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.time.temporal.ChronoField.*;

/**
 * Basic statemachine class, can be used for any Transition typed which extends the transition interface. It contains the current state and a hashmap for events and transitions,
 * which are the minimal requirements to have a working statemachine.
 */
public class BasicStateMachine<T extends IStateMachineTransition<S>, S extends IState> implements IStateMachine<T, S>
{
    /**
     * The lists of transitions and events
     */
    @NotNull
    protected final Map<S, List<T>>               transitionMap;
    @NotNull
    protected final Map<IStateEventType, List<T>> eventTransitionMap;

    /**
     * The current states list of transitions
     */
    protected List<T> currentStateTransitions;

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
     * State history allows tracking of state changes over time, enabled by default in dev
     */
    private boolean     historyEnabled = !FMLEnvironment.production;
    private int         historyIndex   = -1;
    private Component[] stateHistory   = new Component[20];

    DateTimeFormatter SIMPLE_TIME = new DateTimeFormatterBuilder()
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2).toFormatter();

    /**
     * Construct a new StateMachine
     *
     * @param initialState     the initial state.
     * @param exceptionHandler the exception handler.
     */
    protected BasicStateMachine(@NotNull final S initialState, @NotNull final Consumer<RuntimeException> exceptionHandler)
    {
        this.state = initialState;
        this.initState = initialState;
        this.exceptionHandler = exceptionHandler;
        this.transitionMap = new HashMap<>();
        currentStateTransitions = new ArrayList<>();
        this.transitionMap.put(initialState, currentStateTransitions);
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
            eventTransitionMap.get(((IStateMachineEvent<?>) transition).getEventType()).removeIf(t -> t == transition);
        }
        else
        {
            transitionMap.get(transition.getState()).removeIf(t -> t == transition);
        }
    }

    /**
     * Updates the statemachine.
     */
    public void tick()
    {
        for (final List<T> transitions : eventTransitionMap.values())
        {
            for (final T transition : transitions)
            {
                if (checkTransition(transition))
                {
                    return;
                }
            }
        }

        for (final T transition : currentStateTransitions)
        {
            if (checkTransition(transition))
            {
                return;
            }
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
            Log.getLogger().warn("Condition check for state " + getState() + " threw an exception:", e);
            this.onException(e);
            return false;
        }
        return transitionToNext(transition);
    }

    @Override
    public boolean transitionToNext(@NotNull final T transition)
    {
        final S newState;
        try
        {
            newState = transition.getNextState();
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Statemachine for state " + getState() + " threw an exception:", e);
            this.onException(e);
            return false;
        }

        if (newState != null)
        {
            if (transition instanceof IStateMachineOneTimeEvent && ((IStateMachineOneTimeEvent<?>) transition).shouldRemove())
            {
                removeTransition(transition);
            }

            if (newState != state)
            {
                currentStateTransitions = transitionMap.get(newState);

                if (currentStateTransitions == null || currentStateTransitions.isEmpty())
                {
                    // Reached Trap/Sink state we cannot leave.
                    onException(new RuntimeException("Missing AI transition for state: " + newState));
                    reset();
                    return true;
                }

                if (historyEnabled)
                {
                    historyIndex = (historyIndex + 1) % stateHistory.length;
                    stateHistory[historyIndex] = Component.literal(LocalTime.now().format(SIMPLE_TIME) + " ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(transition.getName())
                        .append(Component.literal("->").append(Component.literal(newState.toString()).withStyle(ChatFormatting.LIGHT_PURPLE)));
                }
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
        historyEnabled = true;
        exceptionHandler.accept(e);
    }

    /**
     * Get the current state of the statemachine
     *
     * @return The current IAIState.
     */
    @Override
    public final S getState()
    {
        return state;
    }

    /**
     * Resets the statemachine
     */
    @Override
    public void reset()
    {
        state = initState;
        currentStateTransitions = transitionMap.get(initState);
    }

    @Override
    public void setHistoryEnabled(boolean enabled, final int memorySize)
    {
        if (enabled == historyEnabled && memorySize == stateHistory.length)
        {
            return;
        }

        historyEnabled = enabled;
        stateHistory = new Component[memorySize];
    }

    @Override
    public Component getHistory()
    {
        MutableComponent history = Component.literal("Current state:").append(Component.literal(state + "\n").withStyle(ChatFormatting.GOLD));
        int index = historyIndex;
        for (int i = 0; i < stateHistory.length; i++)
        {
            index = ((index + 1) + stateHistory.length) % stateHistory.length;
            Component entry = stateHistory[index];
            if (entry == null)
            {
                continue;
            }

            history.append(entry).append("\n");
        }

        return history;
    }
}
