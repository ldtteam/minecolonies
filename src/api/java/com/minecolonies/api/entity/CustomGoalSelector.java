package com.minecolonies.api.entity;

import com.google.common.collect.Sets;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.profiler.IProfiler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * A simplified goal selector, for more performance.
 */
public class CustomGoalSelector extends GoalSelector
{
    /**
     * Dummy Goal, used for filling up the list.
     */
    private static final PrioritizedGoal DUMMY = new PrioritizedGoal(Integer.MAX_VALUE, new Goal()
    {
        public boolean shouldExecute()
        {
            return false;
        }
    })
    {
        public boolean isRunning()
        {
            return false;
        }
    };

    /**
     * By vanilla design there is max 1 running goal per flag, which is running is determined by priorities. This array contains the current goal for each flag.
     */
    private final PrioritizedGoal[] flagGoalsArray = new PrioritizedGoal[FLAG_COUNT];

    /**
     * All goals added to this selector
     */
    public Set<PrioritizedGoal> goals = Sets.newHashSet();

    /**
     * Profiler used for debug information /debug
     */
    private IProfiler profiler;

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
        super.goals = this.goals;
        super.profiler = this.profiler;
    }

    /**
     * Creates a new customgoalselector with the given profiler.
     *
     * @param profiler the profiler to use, usually attached to a world object
     */
    public CustomGoalSelector(@NotNull final IProfiler profiler)
    {
        super(profiler);
        this.profiler = profiler;
        super.goals = this.goals;
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
            flagGoalsArray[flag.ordinal()] = selector.flagGoals.getOrDefault(flag, DUMMY);
        }

        // Set goal list reference to existing
        goals = selector.goals;
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
    public void addGoal(int priority, Goal task)
    {
        this.goals.add(new PrioritizedGoal(priority, task));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    public void removeGoal(Goal task)
    {
        this.goals.stream().filter((goal) -> {
            return goal.getGoal() == task;
        }).filter(PrioritizedGoal::isRunning).forEach(PrioritizedGoal::resetTask);
        this.goals.removeIf((goal) -> {
            return goal.getGoal() == task;
        });
    }

    /**
     * Whether the goals flag are within the disabled flags.
     *
     * @param goal
     * @return
     */
    private boolean goalContainsDisabledFlag(final PrioritizedGoal goal)
    {
        for (int i = 0; i < FLAG_COUNT; i++)
        {
            if (disabledFlagsArray[i])
            {
                if (goal.getMutexFlags().contains(Goal.Flag.values()[i]))
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
    private boolean isPreemptedByAll(final PrioritizedGoal goal1)
    {
        for (int i = 0; i < FLAG_COUNT; i++)
        {
            final PrioritizedGoal compareGoal = flagGoalsArray[i];
            if (compareGoal.isRunning() && !compareGoal.isPreemptedBy(goal1) && goal1.getMutexFlags().contains(Goal.Flag.values()[i]))
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
    public void tick()
    {
        this.profiler.startSection("goalUpdate");

        boolean hasFlags;
        counter++;

        for (final PrioritizedGoal currentGoal : goals)
        {
            hasFlags = !currentGoal.getMutexFlags().isEmpty();

            if (currentGoal.isRunning() && (hasFlags && goalContainsDisabledFlag(currentGoal) || !currentGoal.shouldContinueExecuting()))
            {
                currentGoal.resetTask();
            }

            // Vanilla behaviour changed to checking it each tick with 1.14
            if (counter == 1 && !currentGoal.isRunning() &&
                  ((!hasFlags && currentGoal.shouldExecute()) || (!goalContainsDisabledFlag(currentGoal) && isPreemptedByAll(currentGoal) && currentGoal.shouldExecute())))
            {
                for (Goal.Flag flag : currentGoal.getMutexFlags())
                {
                    final PrioritizedGoal prioritizedgoal = flagGoalsArray[flag.ordinal()];
                    prioritizedgoal.resetTask();
                    flagGoalsArray[flag.ordinal()] = currentGoal;
                }
                currentGoal.startExecuting();
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
        this.profiler.endSection();
    }

    /**
     * Gets all goals currently running
     *
     * @return
     */
    public Stream<PrioritizedGoal> getRunningGoals()
    {
        return this.goals.stream().filter(PrioritizedGoal::isRunning);
    }

    /**
     * Disables the given flag
     *
     * @param flag
     */
    public void disableFlag(Goal.Flag flag)
    {
        this.disabledFlagsArray[flag.ordinal()] = true;
    }

    /**
     * Enables the given flag
     *
     * @param flag
     */
    public void enableFlag(Goal.Flag flag)
    {
        this.disabledFlagsArray[flag.ordinal()] = false;
    }

    /**
     * Sets the flag to enabled or disabled
     *
     * @param flag    Flag to set
     * @param enabled enable or disable it
     */
    public void setFlag(Goal.Flag flag, boolean enabled)
    {
        if (enabled)
        {
            this.enableFlag(flag);
        }
        else
        {
            this.disableFlag(flag);
        }
    }
}