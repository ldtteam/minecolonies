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
 * Is triggered when the maximum number of fields has been allocated to a single farmer
 */
public class MaxFieldsTrigger extends SimpleCriterionTrigger<MaxFieldsTrigger.MaxFieldsTriggerInstance>
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
    public Codec<MaxFieldsTriggerInstance> codec()
    {
        return MaxFieldsTriggerInstance.CODEC;
    }

    public static record MaxFieldsTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<MaxFieldsTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(MaxFieldsTriggerInstance::player))
            .apply(builder, MaxFieldsTriggerInstance::new));

        public static Criterion<MaxFieldsTriggerInstance> maxFields()
        {
            return AdvancementTriggers.MAX_FIELDS.get().createCriterion(new MaxFieldsTriggerInstance(Optional.empty()));
        }
    }
}
