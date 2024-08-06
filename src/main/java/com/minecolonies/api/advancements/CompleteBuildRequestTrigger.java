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

public class CompleteBuildRequestTrigger extends SimpleCriterionTrigger<CompleteBuildRequestTrigger.CompleteBuildRequestTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player        the player the check regards
     * @param structureName the structure that was just completed
     * @param level         the level the structure got upgraded to, or 0
     */
    public void trigger(final ServerPlayer player, final String structureName, final int level)
    {
        trigger(player, trigger -> trigger.test(structureName, level));
    }

    @Override
    public Codec<CompleteBuildRequestTriggerInstance> codec()
    {
        return CompleteBuildRequestTriggerInstance.CODEC;
    }

    public static record CompleteBuildRequestTriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> hutName, int level) implements SimpleInstance
    {
        public static final int DEFAULT_LEVEL = -1;

        public static final Codec<CompleteBuildRequestTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CompleteBuildRequestTriggerInstance::player),
                Codec.STRING.optionalFieldOf("hut_name").forGetter(CompleteBuildRequestTriggerInstance::hutName),
              ExtraCodecs.intRange(0, 5).optionalFieldOf("level", DEFAULT_LEVEL).forGetter(CompleteBuildRequestTriggerInstance::level))
            .apply(builder, CompleteBuildRequestTriggerInstance::new));

        public static Criterion<CompleteBuildRequestTriggerInstance> completeBuildRequest()
        {
            return completeBuildRequest(null, DEFAULT_LEVEL);
        }

        /**
         * Construct the check with a single condition
         * 
         * @param hutName the hut that has to be completed to succeed
         */
        public static Criterion<CompleteBuildRequestTriggerInstance> completeBuildRequest(final String hutName)
        {
            return completeBuildRequest(hutName, DEFAULT_LEVEL);
        }

        /**
         * Construct the check with a more specific condition
         * 
         * @param hutName the hut that has to be completed to succeed
         * @param level   the level of the hut that should be completed
         */
        public static Criterion<CompleteBuildRequestTriggerInstance> completeBuildRequest(final String hutName, final int level)
        {
            return AdvancementTriggers.COMPLETE_BUILD_REQUEST.get()
                .createCriterion(new CompleteBuildRequestTriggerInstance(Optional.empty(), Optional.ofNullable(hutName), level));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  structureName the id of the structure that was just built
         * @param  level         the level that the structure is now on, or 0
         * @return               whether the check succeeded
         */
        public boolean test(final String structureName, final int level)
        {
            if (this.hutName.isPresent() && this.level != DEFAULT_LEVEL)
            {
                return this.hutName.get().equalsIgnoreCase(structureName) && this.level <= level;
            }
            else if (this.hutName.isPresent())
            {
                return this.hutName.get().equalsIgnoreCase(structureName);
            }

            return true;
        }
    }
}
