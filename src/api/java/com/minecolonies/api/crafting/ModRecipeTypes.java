package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

public final class ModRecipeTypes
{

    public static final ResourceLocation CLASSIC_ID    = new ResourceLocation(Constants.MOD_ID, "classic");
    public static final ResourceLocation MULTI_OUTPUT_ID    = new ResourceLocation(Constants.MOD_ID, "multi_output");

    public static RecipeTypeEntry Classic;
    public static RecipeTypeEntry MultiOutput;

    private ModRecipeTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
