package com.minecolonies.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public class ColonyPopulationTrigger extends SimpleCriterionTrigger<ColonyPopulationTrigger.ColonyPopulationTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player, final int populationCount)
    {
        trigger(player, trigger -> trigger.test(populationCount));
    }

    @Override
    public Codec<ColonyPopulationTriggerInstance> codec()
    {
        return ColonyPopulationTriggerInstance.CODEC;
    }

    public static record ColonyPopulationTriggerInstance(Optional<ContextAwarePredicate> player, int populationCount) implements SimpleInstance
    {
        public static final Codec<ColonyPopulationTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ColonyPopulationTriggerInstance::player),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("population_count").forGetter(ColonyPopulationTriggerInstance::populationCount))
            .apply(builder, ColonyPopulationTriggerInstance::new));

        /**
         * Constructs a check with a single condition
         * 
         * @param populationCount the population that has to be reached to succeed
         */
        public static Criterion<ColonyPopulationTriggerInstance> colonyPopulation(final int populationCount)
        {
            return AdvancementTriggers.COLONY_POPULATION.get()
                .createCriterion(new ColonyPopulationTriggerInstance(Optional.empty(), populationCount));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  populationCount the current population
         * @return                 whether the check succeeded
         */
        public boolean test(final int populationCount)
        {
            // Less than sign used just in case a previous population increase was missed.
            return this.populationCount <= populationCount;
        }
    }
}
