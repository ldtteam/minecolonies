package com.minecolonies.api.advancements.building_add_recipe;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
    public void trigger(final ServerPlayer player, final IRecipeStorage recipeStorage)
    {
        final BuildingAddRecipeListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(recipeStorage);
        }
    }

    @NotNull
    @Override
    public BuildingAddRecipeCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final DeserializationContext jsonDeserializationContext)
    {
        return BuildingAddRecipeCriterionInstance.deserializeFromJson(jsonObject, jsonDeserializationContext);
    }
}
