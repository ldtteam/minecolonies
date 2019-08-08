package com.minecolonies.coremod.entity.ai.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.minecolonies.api.entity.ai.registry.IMobAIRegistry;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.IArcherMobEntity;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.mobs.aitasks.EntityAIAttackArcher;
import com.minecolonies.coremod.entity.mobs.aitasks.EntityAIRaiderAttackMelee;
import com.minecolonies.coremod.entity.mobs.aitasks.EntityAIWalkToRandomHuts;
import com.minecolonies.coremod.util.MultimapCollector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

public class MobAIRegistry implements IMobAIRegistry
{
    private final List<TaskInformationWrapper<AbstractEntityMinecoloniesMob>> mobAiTasks       = Lists.newArrayList();
    private final List<TaskInformationWrapper<AbstractEntityMinecoloniesMob>> mobAiTargetTasks = Lists.newArrayList();

    public MobAIRegistry()
    {
        setupMobAiTasks(this);
    }

    /**
     * Method setups the AI task logic for mobs.
     * Replaces the old MobSpawnUtils.setAi(Mob)
     *
     * @param registry The registry to register the AI tasks to.
     */
    private static void setupMobAiTasks(IMobAIRegistry registry)
    {
        registry
          .registerNewAiTaskForMobs(PRIORITY_ZERO, SwimGoal::new)
          .registerNewAiTaskForMobs(PRIORITY_FOUR, mob -> new EntityAIWalkToRandomHuts(mob, AI_MOVE_SPEED))
          .registerNewAiTargetTaskForMobs(PRIORITY_TWO, mob -> new NearestAttackableTargetGoal<>(mob, PlayerEntity.class, true))
          .registerNewAiTargetTaskForMobs(PRIORITY_THREE, mob -> new NearestAttackableTargetGoal<>(mob, EntityCitizen.class, true))
          .registerNewAiTaskForMobs(PRIORITY_FIVE, mob -> new LookAtGoal(mob, PlayerEntity.class, MAX_WATCH_DISTANCE))
          .registerNewAiTaskForMobs(PRIORITY_SIX, mob -> new LookAtGoal(mob, EntityCitizen.class, MAX_WATCH_DISTANCE))
          .registerNewAiTaskForMobs(PRIORITY_ONE, EntityAIAttackArcher::new, mob -> mob instanceof IArcherMobEntity)
          .registerNewAiTaskForMobs(PRIORITY_ONE, EntityAIRaiderAttackMelee::new, mob -> !(mob instanceof IArcherMobEntity));
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
    private final class TaskInformationWrapper<M extends Entity>
    {
        private final int                                                   priority;
        private final Function<AbstractEntityMinecoloniesMob, Goal> aiTaskProducer;
        private final Predicate<M>                                          entityPredicate;

        private TaskInformationWrapper(
          final int priority,
          final Function<AbstractEntityMinecoloniesMob, Goal> aiTaskProducer, final Predicate<M> entityPredicate)
        {
            this.priority = priority;
            this.aiTaskProducer = aiTaskProducer;
            this.entityPredicate = entityPredicate;
        }

        public int getPriority()
        {
            return priority;
        }

        public Function<AbstractEntityMinecoloniesMob, Goal> getAiTaskProducer()
        {
            return aiTaskProducer;
        }

        public Predicate<M> getEntityPredicate()
        {
            return entityPredicate;
        }
    }
}
