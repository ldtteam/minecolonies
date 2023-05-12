package com.minecolonies.coremod.entity.ai.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.registry.IMobAIRegistry;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.IArcherMobEntity;
import com.minecolonies.api.entity.mobs.IRangedMobEntity;
import com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.aitasks.EntityAIBreakDoor;
import com.minecolonies.coremod.entity.mobs.aitasks.RaiderMeleeAI;
import com.minecolonies.coremod.entity.mobs.aitasks.RaiderRangedAI;
import com.minecolonies.coremod.entity.mobs.aitasks.RaiderWalkAI;
import com.minecolonies.coremod.util.MultimapCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.RaiderConstants.*;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIInteractToggleAble.FENCE_TOGGLE;

public class MobAIRegistry implements IMobAIRegistry
{
    private final List<TaskInformationWrapper<AbstractEntityMinecoloniesMob, Goal>>     mobAiTasks       = Lists.newArrayList();
    private final List<TaskInformationWrapper<AbstractEntityMinecoloniesMob, Goal>>     mobAiTargetTasks = Lists.newArrayList();
    private final List<TaskInformationWrapper<AbstractEntityMinecoloniesMob, IStateAI>> mobStateAITasks  = Lists.newArrayList();

    public MobAIRegistry()
    {
        setupMobAiTasks(this);
    }

    /**
     * Method setups the AI task logic for mobs. Replaces the old MobSpawnUtils.setAi(Mob)
     *
     * @param registry The registry to register the AI tasks to.
     */
    private static void setupMobAiTasks(final IMobAIRegistry registry)
    {
        registry
          .registerNewAiTaskForMobs(PRIORITY_ZERO, FloatGoal::new)
          .registerNewAiTargetTaskForMobs(PRIORITY_THREE, mob -> new EntityAIInteractToggleAble(mob, FENCE_TOGGLE))
          .registerNewAiTargetTaskForMobs(PRIORITY_THREE, mob -> new EntityAIBreakDoor(mob))
          .registerNewAiTaskForMobs(PRIORITY_FIVE, mob -> new LookAtPlayerGoal(mob, Player.class, MAX_WATCH_DISTANCE))
          .registerNewAiTaskForMobs(PRIORITY_SIX, mob -> new LookAtPlayerGoal(mob, EntityCitizen.class, MAX_WATCH_DISTANCE))
          .registerNewStateAI(mob -> new RaiderMeleeAI<>(mob, mob.getAI()), mob -> !(mob instanceof IArcherMobEntity))
          .registerNewStateAI(mob -> new RaiderRangedAI(mob, mob.getAI()), mob -> mob instanceof IRangedMobEntity)
          .registerNewStateAI(mob -> new RaiderWalkAI(mob, mob.getAI()), mob -> true);
    }

    @NotNull
    @Override
    public Multimap<Integer, Goal> getEntityAiTasksForMobs(final AbstractEntityMinecoloniesMob mob)
    {
        return mobAiTasks.stream().filter(wrapper -> wrapper.entityPredicate.test(mob)).collect(MultimapCollector.toMultimap(
          TaskInformationWrapper::getPriority,
          wrapper -> wrapper.getAiTaskProducer().apply(mob)
          )
        );
    }

    @NotNull
    @Override
    public IMobAIRegistry registerNewAiTaskForMobs(
      final int priority, final Function<AbstractEntityMinecoloniesMob, Goal> aiTaskProducer, final Predicate<AbstractEntityMinecoloniesMob> applyPredicate)
    {
        mobAiTasks.add(new TaskInformationWrapper<>(priority, aiTaskProducer, applyPredicate));
        return this;
    }

    @NotNull
    @Override
    public IMobAIRegistry registerNewStateAI(
      final Function<AbstractEntityMinecoloniesMob, IStateAI> aiTaskProducer, final Predicate<AbstractEntityMinecoloniesMob> applyPredicate)
    {
        mobStateAITasks.add(new TaskInformationWrapper<>(0, aiTaskProducer, applyPredicate));
        return this;
    }

    @NotNull
    @Override
    public void applyToMob(final AbstractEntityMinecoloniesMob mob)
    {
        for (final TaskInformationWrapper<AbstractEntityMinecoloniesMob, IStateAI> task : mobStateAITasks)
        {
            if (task.entityPredicate.test(mob))
            {
                task.aiTaskProducer.apply(mob);
            }
        }

        for (final TaskInformationWrapper<AbstractEntityMinecoloniesMob, Goal> task : mobAiTargetTasks)
        {
            if (task.entityPredicate.test(mob))
            {
                mob.goalSelector.addGoal(task.priority, task.aiTaskProducer.apply(mob));
            }
        }

        for (final TaskInformationWrapper<AbstractEntityMinecoloniesMob, Goal> task : mobAiTasks)
        {
            if (task.entityPredicate.test(mob))
            {
                mob.goalSelector.addGoal(task.priority, task.aiTaskProducer.apply(mob));
            }
        }
    }

    @NotNull
    @Override
    public Multimap<Integer, Goal> getEntityAiTargetTasksForMobs(final AbstractEntityMinecoloniesMob mob)
    {
        return mobAiTargetTasks.stream().filter(wrapper -> wrapper.getEntityPredicate().test(mob)).collect(MultimapCollector.toMultimap(
          TaskInformationWrapper::getPriority,
          wrapper -> wrapper.getAiTaskProducer().apply(mob)
          )
        );
    }

    @NotNull
    @Override
    public IMobAIRegistry registerNewAiTargetTaskForMobs(
      final int priority, final Function<AbstractEntityMinecoloniesMob, Goal> aiTaskProducer, final Predicate<AbstractEntityMinecoloniesMob> applyPredicate)
    {
        mobAiTargetTasks.add(new TaskInformationWrapper<>(priority, aiTaskProducer, applyPredicate));
        return this;
    }

    /**
     * Class that holds registered AI task information.
     *
     * @param <M> The mob type.
     */
    private static final class TaskInformationWrapper<M extends Entity, G>
    {
        private final int                                        priority;
        private final Function<AbstractEntityMinecoloniesMob, G> aiTaskProducer;
        private final Predicate<M>                               entityPredicate;

        TaskInformationWrapper(
          final int priority,
          final Function<AbstractEntityMinecoloniesMob, G> aiTaskProducer, final Predicate<M> entityPredicate)
        {
            this.priority = priority;
            this.aiTaskProducer = aiTaskProducer;
            this.entityPredicate = entityPredicate;
        }

        public int getPriority()
        {
            return priority;
        }

        public Function<AbstractEntityMinecoloniesMob, G> getAiTaskProducer()
        {
            return aiTaskProducer;
        }

        public Predicate<M> getEntityPredicate()
        {
            return entityPredicate;
        }
    }
}
