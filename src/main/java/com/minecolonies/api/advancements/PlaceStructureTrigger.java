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
 * Triggers whenever the build tool is used to position a new structure
 */
public class PlaceStructureTrigger extends SimpleCriterionTrigger<PlaceStructureTrigger.PlaceStructureTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player        the player the check regards
     * @param structureName the structure id of what was just placed
     */
    public void trigger(final ServerPlayer player, final String structureName)
    {
        trigger(player, trigger -> trigger.test(structureName));
    }

    @Override
    public Codec<PlaceStructureTriggerInstance> codec()
    {
        return PlaceStructureTriggerInstance.CODEC;
    }

    public static record PlaceStructureTriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> structureName) implements SimpleInstance
    {
        public static final Codec<PlaceStructureTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlaceStructureTriggerInstance::player),
                Codec.STRING.optionalFieldOf("hut_name").forGetter(PlaceStructureTriggerInstance::structureName))
            .apply(builder, PlaceStructureTriggerInstance::new));

        public static Criterion<PlaceStructureTriggerInstance> placeStructure()
        {
            return placeStructure(null);
        }

        /**
         * Construct the check with a single condition
         * 
         * @param hutName the hut that has to be placed to succeed
         */
        public static Criterion<PlaceStructureTriggerInstance> placeStructure(final String hutName)
        {
            return AdvancementTriggers.PLACE_STRUCTURE.get()
                .createCriterion(new PlaceStructureTriggerInstance(Optional.empty(), Optional.ofNullable(hutName)));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  hutName the id of the structure that was just placed
         * @return         whether the check succeeded
         */
        public boolean test(final String hutName)
        {
            if (this.structureName.isPresent())
            {
                return this.structureName.get().equalsIgnoreCase(hutName);
            }

            return true;
        }
    }
}
