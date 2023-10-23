package com.minecolonies.api.entity.ai.registry;

import com.google.common.collect.Multimap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public interface IMobAIRegistry
{
    static IMobAIRegistry getInstance() { return IMinecoloniesAPI.getInstance().getMobAIRegistry(); }

    /**
     * Method to get the AI tasks registered for a given mob. Used by minecolonies to get the AIs that are required for a given mob.
     *
     * @param mob The mob that the system is initializing and requests the AI for.
     * @return The map with the entity AI tasks that are needed for the given mob, with their priorities.
     */
    @NotNull
    Multimap<Integer, Goal> getEntityAiTasksForMobs(final AbstractEntityRaiderMob mob);

    /**
     * Method used to register a entity AI task for a mob that matches the predicate.
     *
     * @param priority       The priority to register this task on.
     * @param aiTaskProducer The task producer in question to register.
     * @return The registry.
     */
    @NotNull
    default IMobAIRegistry registerNewAiTaskForMobs(final int priority, final Function<AbstractEntityRaiderMob, Goal> aiTaskProducer)
    {
        return this.registerNewAiTaskForMobs(priority, aiTaskProducer, mob -> true);
    }

    /**
     * Method used to register a entity AI task for a mob that matches the predicate.
     *
     * @param priority       The priority to register this task on.
     * @param aiTaskProducer The task producer in question to register.
     * @param applyPredicate The predicate used to indicate if the task should be applied to a given mob.
     * @return The registry.
     */
    @NotNull
    IMobAIRegistry registerNewAiTaskForMobs(
      final int priority,
      final Function<AbstractEntityRaiderMob, Goal> aiTaskProducer,
      Predicate<AbstractEntityRaiderMob> applyPredicate);

    /**
     * Method used to register a entity AI task for a mob that matches the predicate.
     *
     * @param aiTaskProducer The task producer in question to register.
     * @param applyPredicate The predicate used to indicate if the task should be applied to a given mob.
     * @return The registry.
     */
    @NotNull
    IMobAIRegistry registerNewStateAI(
      final Function<AbstractEntityRaiderMob, IStateAI> aiTaskProducer,
      Predicate<AbstractEntityRaiderMob> applyPredicate);

    /**
     * Applies the registered AI's to the given mob
     */
    @NotNull
    void applyToMob(AbstractEntityRaiderMob mob);

    /**
     * Method to get the AI target tasks registered for a given mob. Used by minecolonies to get the AIs that are required for a given mob.
     *
     * @param mob The mob that the system is initializing and requests the AI for.
     * @return The map with the entity AI tasks that are needed for the given mob, with their priorities.
     */
    @NotNull
    Multimap<Integer, Goal> getEntityAiTargetTasksForMobs(final AbstractEntityRaiderMob mob);

    /**
     * Method used to register a entity AI target task for a mob that matches the predicate.
     *
     * @param priority       The priority to register this task on.
     * @param aiTaskProducer The task producer in question to register.
     * @return The registry.
     */
    @NotNull
    default IMobAIRegistry registerNewAiTargetTaskForMobs(final int priority, final Function<AbstractEntityRaiderMob, Goal> aiTaskProducer)
    {
        return this.registerNewAiTargetTaskForMobs(priority, aiTaskProducer, mob -> true);
    }

    /**
     * Method used to register a entity AI target task for a mob that matches the predicate.
     *
     * @param priority       The priority to register this task on.
     * @param aiTaskProducer The task producer in question to register.
     * @param applyPredicate The predicate used to indicate if the task should be applied to a given mob.
     * @return The registry.
     */
    @NotNull
    IMobAIRegistry registerNewAiTargetTaskForMobs(
      final int priority,
      final Function<AbstractEntityRaiderMob, Goal> aiTaskProducer,
      Predicate<AbstractEntityRaiderMob> applyPredicate);
}
