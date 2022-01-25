package com.minecolonies.api.advancements.building_add_recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the various conditions for "building_add_recipe"
 */
public class BuildingAddRecipeCriterionInstance extends CriterionInstance
{
    private ItemPredicate[] outputItemPredicates;
    private int             craftingSize = -1;

    /**
     * Default instance when no conditions are applied to the trigger
     */
    public BuildingAddRecipeCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE), EntityPredicate.AndPredicate.ANY);
    }

    /**
     * Instance with the condition to check what item recipe was added
     * @param outputItemPredicates the item recipe tester constructed from the advancement information
     */
    public BuildingAddRecipeCriterionInstance(final ItemPredicate[] outputItemPredicates)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE), EntityPredicate.AndPredicate.ANY);

        this.outputItemPredicates = outputItemPredicates;
    }

    /**
     * Instance with the condition to check what item recipe was added and at what grid size
     * @param outputItemPredicates the item recipe tester constructed from the advancement information
     * @param craftingSize the NxN size of the crafting grid
     */
    public BuildingAddRecipeCriterionInstance(final ItemPredicate[] outputItemPredicates, final int craftingSize)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE), EntityPredicate.AndPredicate.ANY);

        this.outputItemPredicates = outputItemPredicates;
        this.craftingSize = craftingSize;
    }

    /**
     * Performs the check for these criteria
     * @param recipeStorage the recipe that was just added
     * @return whether the check succeeded
     */
    public boolean test(final IRecipeStorage recipeStorage)
    {
        if (this.outputItemPredicates != null)
        {
            boolean outputMatches = false;
            for (ItemPredicate itemPredicate : outputItemPredicates)
            {
                if (itemPredicate.matches(recipeStorage.getPrimaryOutput()))
                {
                    outputMatches = true;
                    break;
                }
            }

            if (this.craftingSize != -1)
            {
                return outputMatches && this.craftingSize == recipeStorage.getGridSize();
            }

            return outputMatches;
        }

        return true;
    }

    @NotNull
    public static BuildingAddRecipeCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                         @NotNull final ConditionArrayParser conditions)
    {
        if (jsonObject.has("items"))
        {
            final ItemPredicate[] outputItemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("items"));
            if (jsonObject.has("crafting_size"))
            {
                final int craftingSize = JSONUtils.getAsInt(jsonObject, "crafting_size");
                return new BuildingAddRecipeCriterionInstance(outputItemPredicates, craftingSize);
            }
            return new BuildingAddRecipeCriterionInstance(outputItemPredicates);
        }
        return new BuildingAddRecipeCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final ConditionArraySerializer serializer)
    {
        final JsonObject json = super.serializeToJson(serializer);
        if (this.outputItemPredicates != null && this.outputItemPredicates.length > 0)
        {
            final JsonArray outputItemPredicates = new JsonArray();
            for (ItemPredicate predicate : this.outputItemPredicates)
            {
                outputItemPredicates.add(predicate.serializeToJson());
            }
            json.add("items", outputItemPredicates);
        }
        if (this.craftingSize >= 0)
        {
            json.addProperty("crafting_size", this.craftingSize);
        }
        return json;
    }
}
