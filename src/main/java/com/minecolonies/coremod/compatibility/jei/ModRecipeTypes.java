package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.crafting.CompostRecipe;
import mezz.jei.api.recipe.RecipeType;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModRecipeTypes
{
    public static final RecipeType<CompostRecipe> COMPOSTING =
        RecipeType.create(MOD_ID, "composting", CompostRecipe.class);

    public static final RecipeType<FishermanRecipeCategory.FishingRecipe> FISHING =
            RecipeType.create(MOD_ID, "fishing", FishermanRecipeCategory.FishingRecipe.class);

    private ModRecipeTypes()
    {
        // purely static
    }
}
