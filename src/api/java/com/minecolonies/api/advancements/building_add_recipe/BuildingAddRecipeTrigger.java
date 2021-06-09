package com.minecolonies.api.advancements.building_add_recipe;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered whenever a new recipe has been set in any building
 */
public class BuildingAddRecipeTrigger extends AbstractCriterionTrigger<BuildingAddRecipeListeners, BuildingAddRecipeCriterionInstance>
{
    public BuildingAddRecipeTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE), BuildingAddRecipeListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param recipeStorage details about the recipe that was added
     */
    public void trigger(final ServerPlayerEntity player, final IRecipeStorage recipeStorage)
    {
        final BuildingAddRecipeListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(recipeStorage);
        }
    }

    @NotNull
    @Override
    public BuildingAddRecipeCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final ConditionArrayParser jsonDeserializationContext)
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
}
