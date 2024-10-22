package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.core.colony.crafting.ToolUsage;
import mezz.jei.api.recipe.RecipeType;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModRecipeTypes
{
    public static final RecipeType<CropRecipeCategory.CropRecipe> CROPS =
        RecipeType.create(MOD_ID, "crops", CropRecipeCategory.CropRecipe.class);

    public static final RecipeType<CompostRecipe> COMPOSTING =
            RecipeType.create(MOD_ID, "composting", CompostRecipe.class);

    public static final RecipeType<FishermanRecipeCategory.FishingRecipe> FISHING =
            RecipeType.create(MOD_ID, "fishing", FishermanRecipeCategory.FishingRecipe.class);

    public static final RecipeType<ToolUsage> TOOLS =
            RecipeType.create(MOD_ID, "tools", ToolUsage.class);

    private ModRecipeTypes()
    {
        // purely static
    }
}
