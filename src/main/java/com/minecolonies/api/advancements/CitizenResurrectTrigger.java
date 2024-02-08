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
 * A Trigger that is triggered when the miner reaches a certain depth
 */
public class CitizenResurrectTrigger extends SimpleCriterionTrigger<CitizenResurrectTrigger.CitizenResurrectTriggerInstance>
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
    public Codec<CitizenResurrectTriggerInstance> codec()
    {
        return CitizenResurrectTriggerInstance.CODEC;
    }

    public static record CitizenResurrectTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<CitizenResurrectTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(CitizenResurrectTriggerInstance::player))
            .apply(builder, CitizenResurrectTriggerInstance::new));

        public static Criterion<CitizenResurrectTriggerInstance> citizenResurrect()
        {
            return AdvancementTriggers.CITIZEN_RESURRECT.get().createCriterion(new CitizenResurrectTriggerInstance(Optional.empty()));
        }
    }
}
