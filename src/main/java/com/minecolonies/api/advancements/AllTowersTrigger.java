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
 * Triggered when all barracks towers have been fully upgraded on any one barracks
 */
public class AllTowersTrigger extends SimpleCriterionTrigger<AllTowersTrigger.AllTowersTriggerInstance>
{
    /**
     * Triggers the listener checks if there is any listening in
     * 
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player)
    {
        trigger(player, trigger -> true);
    }

    @Override
    public Codec<AllTowersTriggerInstance> codec()
    {
        return AllTowersTriggerInstance.CODEC;
    }

    public static record AllTowersTriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance
    {
        public static final Codec<AllTowersTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(AllTowersTriggerInstance::player))
            .apply(builder, AllTowersTriggerInstance::new));

        public static Criterion<AllTowersTriggerInstance> allTowers()
        {
            return AdvancementTriggers.ALL_TOWERS.get().createCriterion(new AllTowersTriggerInstance(Optional.empty()));
        }
    }
}
