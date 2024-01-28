package com.minecolonies.api.advancements.building_add_recipe;

import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.crafting.IRecipeStorage;
import net.minecraft.server.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class BuildingAddRecipeListeners extends CriterionListeners<BuildingAddRecipeCriterionInstance>
{
    public BuildingAddRecipeListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final IRecipeStorage recipeStorage)
    {
        trigger(instance -> instance.test(recipeStorage));
    }
}
