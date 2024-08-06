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
 * A Trigger that is triggered when the undertaker recieves a totem of undying
 */
public class UndertakerTotemTrigger extends SimpleCriterionTrigger<UndertakerTotemTrigger.UndertakerTotemTriggerInstance>
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
    public Codec<UndertakerTotemTriggerInstance> codec()
    {
        return UndertakerTotemTriggerInstance.CODEC;
    }

    public static record UndertakerTotemTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<UndertakerTotemTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UndertakerTotemTriggerInstance::player))
            .apply(builder, UndertakerTotemTriggerInstance::new));

        public static Criterion<UndertakerTotemTriggerInstance> undertakerTotem()
        {
            return AdvancementTriggers.UNDERTAKER_TOTEM.get().createCriterion(new UndertakerTotemTriggerInstance(Optional.empty()));
        }
    }
}
