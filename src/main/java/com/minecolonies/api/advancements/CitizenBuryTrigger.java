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
 * A Trigger that is triggered when a citizen is buried in a graveyard
 */
public class CitizenBuryTrigger extends SimpleCriterionTrigger<CitizenBuryTrigger.CitizenBuryTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player)
    {
        trigger(player, trigger -> true);
    }

    @Override
    public Codec<CitizenBuryTriggerInstance> codec()
    {
        return CitizenBuryTriggerInstance.CODEC;
    }

    public static record CitizenBuryTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<CitizenBuryTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CitizenBuryTriggerInstance::player))
            .apply(builder, CitizenBuryTriggerInstance::new));

        public static Criterion<CitizenBuryTriggerInstance> citizenBury()
        {
            return AdvancementTriggers.CITIZEN_BURY.get().createCriterion(new CitizenBuryTriggerInstance(Optional.empty()));
        }
    }
}
