package com.minecolonies.api.entity;

import com.google.common.collect.Sets;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A simplified goal selector, for more performance.
 */
public class CustomGoalSelector extends GoalSelector
{
    /**
     * Dummy Goal, used for filling up the list.
     */
    private static final WrappedGoal DUMMY = new WrappedGoal(Integer.MAX_VALUE, new Goal()
    {
        @Override
        public boolean canUse()
        {
            return false;
        }
    })
    {
        @Override
        public boolean isRunning()
        {
            return false;
        }
    };

    /**
     * By vanilla design there is max 1 running goal per flag, which is running is determined by priorities. This array contains the current goal for each flag.
     */
    private final WrappedGoal[] flagGoalsArray = new WrappedGoal[FLAG_COUNT];

    /**
     * All goals added to this selector
     */
    public Set<WrappedGoal> availableGoals = Sets.newHashSet();

    /**
     * Profiler used for debug information /debug
     */
    private Supplier<ProfilerFiller> profiler;

    /**
     * Array of flags, true if currently disabled
     */
    private final boolean[] disabledFlagsArray = new boolean[FLAG_COUNT];

    /**
     * Amount of flags.
     */
    private static final int FLAG_COUNT = Goal.Flag.values().length;

    /**
     * Tick counter
     */
    int counter = 0;

    /**
     * Create a new goalselector from an existing one, simply re-uses the references.
     *
     * @param old the old selector to use
     */
    public CustomGoalSelector(@NotNull final GoalSelector old)
    {
        super(old.profiler);
        importFrom(old);
        super.availableGoals = this.availableGoals;
        super.profiler = this.profiler;
    }

    /**
     * Creates a new customgoalselector with the given profiler.
     *
     * @param profiler the profiler to use, usually attached to a world object
     */
    public CustomGoalSelector(@NotNull final Supplier<ProfilerFiller> profiler)
    {
        super(profiler);
        this.profiler = profiler;
        super.availableGoals = this.availableGoals;
        super.profiler = this.profiler;
        for (Goal.Flag flag : Goal.Flag.values())
        {
            flagGoalsArray[flag.ordinal()] = DUMMY;
        }
    }

    /**
     * Imports values from another selector
     *
     * @param selector selector to import from
     */
    public void importFrom(final GoalSelector selector)
    {
        if (selector == null)
        {
            return;
        }

        // import current goals for flags
        for (Goal.Flag flag : Goal.Flag.values())
        {
            flagGoalsArray[flag.ordinal()] = selector.lockedFlags.getOrDefault(flag, DUMMY);
        }

        // Set goal list reference to existing
        availableGoals = selector.availableGoals;
        // Set profiler reference
        profiler = selector.profiler;

        // Set which flags are disabled
        for (Goal.Flag flag : selector.disabledFlags)
        {
            disabledFlagsArray[flag.ordinal()] = true;
        }
    }

    /**
     * Add a now AITask. Args : priority, task
     */
    @Override
    public void addGoal(int priority, Goal task)
    {
        this.availableGoals.add(new WrappedGoal(priority, task));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    @Override
    public void removeGoal(Goal task)
    {
        for(final WrappedGoal prioritizedGoal : new ArrayList<>(availableGoals))
        {
            if (prioritizedGoal.getGoal() == task)
            {
                prioritizedGoal.stop();
                availableGoals.remove(prioritizedGoal);
            }
        }
    }

    /**
     * Whether one of the goals flags is within the disabled flags.
     *
     * @param goal the goal to check.
     * @return whether one of the goals flags is within the disabled flags.
     */
    private boolean goalContainsDisabledFlag(final WrappedGoal goal)
    {
        for (int i = 0; i < FLAG_COUNT; i++)
        {
            if (disabledFlagsArray[i])
            {
                if (goal.getFlags().contains(Goal.Flag.values()[i]))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the given goal is higher priority in all flags it uses than existing running goals.
     *
     * @param goal1 goal to check
     * @return true if it overrules the existing goal.
     */
    private boolean isPreemptedByAll(final WrappedGoal goal1)
    {
        for (int i = 0; i < FLAG_COUNT; i++)
        {
            final WrappedGoal compareGoal = flagGoalsArray[i];
            if (compareGoal.isRunning() && !compareGoal.canBeReplacedBy(goal1) && goal1.getFlags().contains(Goal.Flag.values()[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Ticks this selector, first checks running goals to stop, then checks all goals to see which should start running. Finally ticks all running goals Performance wise this is
     * about 6 times faster, when checking at the same rate as the vanilla one, resulting in about 3-4 times less time spent updating and executing AI goals. When updating
     * non-running goals only every 4 ticks it goes up to about 10% of vanilla's time spent for the whole update goals and their execution.
     */
    @Override
    public void tick()
    {
        this.profiler.get().push("goalUpdate");

        boolean hasFlags;
        counter++;

        for (final WrappedGoal currentGoal : new ArrayList<>(availableGoals))
        {
            hasFlags = !currentGoal.getFlags().isEmpty();

            if (currentGoal.isRunning() && (hasFlags && goalContainsDisabledFlag(currentGoal) || !currentGoal.canContinueToUse()))
            {
                currentGoal.stop();
            }

            // Vanilla behaviour changed to checking it each tick with 1.14
            if (counter == 1 && !currentGoal.isRunning() &&
                  ((!hasFlags && currentGoal.canUse()) || (!goalContainsDisabledFlag(currentGoal) && isPreemptedByAll(currentGoal) && currentGoal.canUse())))
            {
                for (Goal.Flag flag : currentGoal.getFlags())
                {
                    final WrappedGoal prioritizedgoal = flagGoalsArray[flag.ordinal()];
                    prioritizedgoal.stop();
                    flagGoalsArray[flag.ordinal()] = currentGoal;
                }
                currentGoal.start();
            }

            if (currentGoal.isRunning())
            {
                currentGoal.tick();
            }
        }

        if (counter > 3)
        {
            counter = 0;
        }
        this.profiler.get().pop();
    }

    /**
     * Gets all goals currently running
     *
     * @return the stream of running goals.
     */
    @Override
    public Stream<WrappedGoal> getRunningGoals()
    {
        return this.availableGoals.stream().filter(WrappedGoal::isRunning);
    }

    /**
     * Disables the given flag
     *
     * @param flag the flag to disable.
     */
    @Override
    public void disableControlFlag(Goal.Flag flag)
    {
        this.disabledFlagsArray[flag.ordinal()] = true;
    }

    /**
     * Enables the given flag
     *
     * @param flag the flag to enable.
     */
    @Override
    public void enableControlFlag(Goal.Flag flag)
    {
        this.disabledFlagsArray[flag.ordinal()] = false;
    }

    /**
     * Sets the flag to enabled or disabled
     *
     * @param flag    Flag to set
     * @param enabled enable or disable it
     */
    @Override
    public void setControlFlag(Goal.Flag flag, boolean enabled)
    {
        if (enabled)
        {
            this.enableControlFlag(flag);
        }
        else
        {
            this.disableControlFlag(flag);
        }
    }
}