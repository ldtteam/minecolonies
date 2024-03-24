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
 * A Trigger for any building request that gets made
 */
public class CreateBuildRequestTrigger extends SimpleCriterionTrigger<CreateBuildRequestTrigger.CreateBuildRequestTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player        the player the check regards
     * @param structureName the structure that is to be created
     * @param level         the level that the request will complete
     */
    public void trigger(final ServerPlayer player, final String structureName, final int level)
    {
        trigger(player, trigger -> trigger.test(structureName, level));
    }

    @Override
    public Codec<CreateBuildRequestTriggerInstance> codec()
    {
        return CreateBuildRequestTriggerInstance.CODEC;
    }

    public static record CreateBuildRequestTriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> hutName, int level) implements SimpleInstance
    {
        public static final int DEFAULT_LEVEL = -1;

        public static final Codec<CreateBuildRequestTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(CreateBuildRequestTriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.STRING, "hut_name").forGetter(CreateBuildRequestTriggerInstance::hutName),
                ExtraCodecs.strictOptionalField(ExtraCodecs.intRange(0, 5), "level", DEFAULT_LEVEL).forGetter(CreateBuildRequestTriggerInstance::level))
            .apply(builder, CreateBuildRequestTriggerInstance::new));

        public static Criterion<CreateBuildRequestTriggerInstance> createBuildRequest()
        {
            return createBuildRequest(null);
        }

        /**
         * Construct the check with a single condition
         * 
         * @param hutName the hut that has to be requested to succeed
         */
        public static Criterion<CreateBuildRequestTriggerInstance> createBuildRequest(final String hutName)
        {
            return createBuildRequest(hutName, DEFAULT_LEVEL);
        }

        /**
         * Construct the check with a more specific condition
         * 
         * @param hutName the hut that has to be requested to succeed
         * @param level   the level that the request should complete
         */
        public static Criterion<CreateBuildRequestTriggerInstance> createBuildRequest(final String hutName, final int level)
        {
            return AdvancementTriggers.CREATE_BUILD_REQUEST.get()
                .createCriterion(new CreateBuildRequestTriggerInstance(Optional.empty(), Optional.ofNullable(hutName), level));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  structureName the id of the structure that was just requested
         * @param  level         the level that the structure will be once completed, or 0
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
