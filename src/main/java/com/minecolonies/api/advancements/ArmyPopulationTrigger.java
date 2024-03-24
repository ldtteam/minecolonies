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

/**
 * Triggered on allocation of new soldiers
 */
public class ArmyPopulationTrigger extends SimpleCriterionTrigger<ArmyPopulationTrigger.ArmyPopulationTriggerInstance>
{
    /**
     * Triggers the listener checks if there is any listening in
     * 
     * @param player   the player the check regards
     * @param armySize the related colony's current army size
     */
    public void trigger(final ServerPlayer player, final int armySize)
    {
        trigger(player, trigger -> trigger.test(armySize));
    }

    @Override
    public Codec<ArmyPopulationTriggerInstance> codec()
    {
        return ArmyPopulationTriggerInstance.CODEC;
    }

    public static record ArmyPopulationTriggerInstance(Optional<ContextAwarePredicate> player, int populationCount) implements SimpleInstance
    {
        public static final Codec<ArmyPopulationTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ArmyPopulationTriggerInstance::player),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("population_count").forGetter(ArmyPopulationTriggerInstance::populationCount))
            .apply(builder, ArmyPopulationTriggerInstance::new));

        /**
         * Registers a deserialized advancement trigger with this criterion condition
         * 
         * @param populationCount the current army size
         */
        public static Criterion<ArmyPopulationTriggerInstance> armyPopulation(final int populationCount)
        {
            return AdvancementTriggers.ARMY_POPULATION.get()
                .createCriterion(new ArmyPopulationTriggerInstance(Optional.empty(), populationCount));
        }

        /**
         * Perform the check comparing the current population to the advancement condition
         * 
         * @param  populationCount the current population
         * @return                 whether the check passed
         */
        public boolean test(final int populationCount)
        {
            // Less than sign used just in case a previous population increase was missed.
            return this.populationCount <= populationCount;
        }
    }
}
