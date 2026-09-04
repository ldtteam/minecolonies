package com.minecolonies.api.crafting;

import com.minecolonies.api.crafting.registry.RecipeTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipeTypes
{

    public static final Identifier CLASSIC_ID    = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "classic");
    public static final Identifier MULTI_OUTPUT_ID    = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "multi_output");

    public static DeferredHolder<RecipeTypeEntry, RecipeTypeEntry> Classic;
    public static DeferredHolder<RecipeTypeEntry, RecipeTypeEntry> MultiOutput;

    private ModRecipeTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
