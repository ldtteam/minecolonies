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
 * Triggered when a supply camp or supply ship has been placed
 */
public class PlaceSupplyTrigger extends SimpleCriterionTrigger<PlaceSupplyTrigger.PlaceSupplyTriggerInstance>
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
    public Codec<PlaceSupplyTriggerInstance> codec()
    {
        return PlaceSupplyTriggerInstance.CODEC;
    }

    public static record PlaceSupplyTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<PlaceSupplyTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlaceSupplyTriggerInstance::player))
            .apply(builder, PlaceSupplyTriggerInstance::new));



        public static Criterion<PlaceSupplyTriggerInstance> placeSupply()
        {
            return AdvancementTriggers.PLACE_SUPPLY.get().createCriterion(new PlaceSupplyTriggerInstance(Optional.empty()));
        }
    }
}
