package com.minecolonies.api.advancements;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Triggered whenever a new recipe has been set in any building
 */
public class BuildingAddRecipeTrigger extends SimpleCriterionTrigger<BuildingAddRecipeTrigger.BuildingAddRecipeTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player        the player the check regards
     * @param recipeStorage details about the recipe that was added
     */
    public void trigger(final ServerPlayer player, final IRecipeStorage recipeStorage)
    {
        trigger(player, trigger -> trigger.test(recipeStorage));
    }

    @Override
    public Codec<BuildingAddRecipeTriggerInstance> codec()
    {
        return BuildingAddRecipeTriggerInstance.CODEC;
    }

    public static record BuildingAddRecipeTriggerInstance(Optional<ContextAwarePredicate> player, List<ItemPredicate> outputItemPredicates, int craftingSize) implements SimpleInstance
    {
        public static final int DEFAULT_CRAFTING_SIZE = -1;
        public static final List<ItemPredicate> DEFAULT_OUTPUT_ITEM_PREDICATES = Collections.emptyList();

        public static final Codec<BuildingAddRecipeTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BuildingAddRecipeTriggerInstance::player),
              ItemPredicate.CODEC.listOf().optionalFieldOf("items", DEFAULT_OUTPUT_ITEM_PREDICATES).forGetter(BuildingAddRecipeTriggerInstance::outputItemPredicates),
              ExtraCodecs.intRange(0, 10).optionalFieldOf("crafting_size", DEFAULT_CRAFTING_SIZE).forGetter(BuildingAddRecipeTriggerInstance::craftingSize))
            .apply(builder, BuildingAddRecipeTriggerInstance::new));

        /**
         * Default instance when no conditions are applied to the trigger
         */
        public static Criterion<BuildingAddRecipeTriggerInstance> buildingAddRecipe()
        {
            return buildingAddRecipe(DEFAULT_OUTPUT_ITEM_PREDICATES, DEFAULT_CRAFTING_SIZE);
        }

        /**
         * Instance with the condition to check what item recipe was added
         * 
         * @param outputItemPredicates the item recipe tester constructed from the advancement information
         */
        public static Criterion<BuildingAddRecipeTriggerInstance> buildingAddRecipe(final List<ItemPredicate> outputItemPredicates)
        {
            return buildingAddRecipe(outputItemPredicates, DEFAULT_CRAFTING_SIZE);
        }

        /**
         * Instance with the condition to check what item recipe was added and at what grid size
         * 
         * @param outputItemPredicates the item recipe tester constructed from the advancement information
         * @param craftingSize         the NxN size of the crafting grid
         */
        public static Criterion<BuildingAddRecipeTriggerInstance> buildingAddRecipe(final List<ItemPredicate> outputItemPredicates,
            final int craftingSize)
        {
            return AdvancementTriggers.BUILDING_ADD_RECIPE.get()
                .createCriterion(new BuildingAddRecipeTriggerInstance(Optional.empty(), outputItemPredicates, craftingSize));
        }

        /**
         * Performs the check for these criteria
         * 
         * @param  recipeStorage the recipe that was just added
         * @return               whether the check succeeded
         */
        public boolean test(final IRecipeStorage recipeStorage)
        {
            if (!this.outputItemPredicates.isEmpty())
            {
                boolean outputMatches = false;
                for (ItemPredicate itemPredicate : outputItemPredicates)
                {
                    if (itemPredicate.test(recipeStorage.getPrimaryOutput()))
                    {
                        outputMatches = true;
                        break;
                    }
                }

                if (this.craftingSize != DEFAULT_CRAFTING_SIZE)
                {
                    return outputMatches && this.craftingSize == recipeStorage.getGridSize();
                }

                return outputMatches;
            }

            return true;
        }
    }
}
