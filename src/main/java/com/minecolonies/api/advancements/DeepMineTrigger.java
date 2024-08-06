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
public class DeepMineTrigger extends SimpleCriterionTrigger<DeepMineTrigger.DeepMineTriggerInstance>
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
    public Codec<DeepMineTriggerInstance> codec()
    {
        return DeepMineTriggerInstance.CODEC;
    }

    public static record DeepMineTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<DeepMineTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DeepMineTriggerInstance::player))
            .apply(builder, DeepMineTriggerInstance::new));

        public static Criterion<DeepMineTriggerInstance> deepMine()
        {
            return AdvancementTriggers.DEEP_MINE.get().createCriterion(new DeepMineTriggerInstance(Optional.empty()));
        }
    }
}
