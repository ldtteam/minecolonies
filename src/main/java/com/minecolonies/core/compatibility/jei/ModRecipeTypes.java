package com.minecolonies.core.compatibility.jei;

import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.core.colony.crafting.ToolUsage;
import mezz.jei.api.recipe.types.IRecipeType;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModRecipeTypes
{
    public static final IRecipeType<CropRecipeCategory.CropRecipe> CROPS =
        IRecipeType.create(MOD_ID, "crops", CropRecipeCategory.CropRecipe.class);

    public static final IRecipeType<CompostRecipe> COMPOSTING =
            IRecipeType.create(MOD_ID, "composting", CompostRecipe.class);

    public static final IRecipeType<FishermanRecipeCategory.FishingRecipe> FISHING =
            IRecipeType.create(MOD_ID, "fishing", FishermanRecipeCategory.FishingRecipe.class);

    public static final IRecipeType<ToolUsage> TOOLS =
            IRecipeType.create(MOD_ID, "tools", ToolUsage.class);

    public static final IRecipeType<FloristRecipeCategory.FloristRecipe> FLOWERS =
            IRecipeType.create(MOD_ID, "flowers", FloristRecipeCategory.FloristRecipe.class);

    private ModRecipeTypes()
    {
        // purely static
    }
}
